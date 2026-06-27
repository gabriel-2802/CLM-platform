"use client"

import { useState, useEffect, useMemo, useRef } from "react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { AlertCircle, FileEdit, Loader2, Save } from "lucide-react"
import { getClientTemplateFields, getTemplateById, updateTemplateMappings, type TemplateField, type TemplateMappingOption } from "@/actions/contract-templates"
import { toast } from "sonner"
import parse, { Element, HTMLReactParserOptions } from "html-react-parser"
import { useSession } from "next-auth/react"
import { API_BASE_URL } from "@/lib/config/public"

const MANUAL_MAPPING_OPTION = { label: "Manual (Input utilizator)", value: "MANUAL" }

// Regex to match placeholder patterns: 4+ dots, Unicode ellipsis, or long underscores
const PLACEHOLDER_REGEX = /\.{4,}|…+|_{5,}/g

export function TemplateMappingModal({
                                       templateId,
                                       templateName,
                                     }: {
  templateId: number
  templateName: string
  fullyMapped?: boolean
}) {
  const { data: session } = useSession()
  const token = (session?.user as { serviceToken?: string } | undefined)?.serviceToken
  const [open, setOpen] = useState(false)
  const [fields, setFields] = useState<TemplateField[]>([])
  const [mappingOptions, setMappingOptions] = useState<TemplateMappingOption[]>([MANUAL_MAPPING_OPTION])
  const [htmlContent, setHtmlContent] = useState<string>("")
  // mappingTypes: fieldId → selected option ("MANUAL" or a client field key)
  // manualLabels: fieldId → custom display name when option is "MANUAL"
  const [mappingTypes, setMappingTypes] = useState<Record<number, string>>({})
  const [manualLabels, setManualLabels] = useState<Record<number, string>>({})
  const [requiredFields, setRequiredFields] = useState<Record<number, boolean>>({})
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [docError, setDocError] = useState<string | null>(null)
  const mappingTypesRef = useRef(mappingTypes)
  mappingTypesRef.current = mappingTypes
  const manualLabelsRef = useRef(manualLabels)
  manualLabelsRef.current = manualLabels
  const requiredFieldsRef = useRef(requiredFields)
  requiredFieldsRef.current = requiredFields

  useEffect(() => {
    if (!open) return

    setLoading(true)
    setDocError(null)
    setHtmlContent("")
    setFields([])

    const fetchData = async () => {
      try {
        // 1. Fetch template metadata and the dynamic client fields exposed by client-service.
        const [templateData, clientFields] = await Promise.all([
          getTemplateById(templateId),
          getClientTemplateFields(),
        ])
        setMappingOptions([MANUAL_MAPPING_OPTION, ...clientFields])

        if (templateData?.fields) {
          const sorted = [...templateData.fields].sort(
              (a, b) => (a.fieldPosition || 0) - (b.fieldPosition || 0)
          )
          setFields(sorted)
          const clientFieldValues = new Set(clientFields.map((cf) => cf.value))
          const initialTypes: Record<number, string> = {}
          const initialLabels: Record<number, string> = {}
          const initialRequired: Record<number, boolean> = {}
          sorted.forEach((f) => {
            const label = f.fieldLabel || "MANUAL"
            if (label !== "MANUAL" && clientFieldValues.has(label)) {
              initialTypes[f.id] = label
              initialLabels[f.id] = ""
            } else {
              initialTypes[f.id] = "MANUAL"
              initialLabels[f.id] = label === "MANUAL" ? "" : label
            }
            initialRequired[f.id] = f.isRequired ?? true
          })
          setMappingTypes(initialTypes)
          setManualLabels(initialLabels)
          setRequiredFields(initialRequired)
        }

        // 2. Fetch the DOCX and convert with mammoth
        if (!token) {
          throw new Error("Autentificare lipsa.")
        }

        const response = await fetch(`${API_BASE_URL}/api/templates/download/${templateId}/docx`, {
          cache: "no-store",
          headers: { Authorization: `Bearer ${token}` },
        })
        if (!response.ok) {
          const errText = await response.text()
          throw new Error(`Eroare la descărcarea fișierului (${response.status}): ${errText}`)
        }

        const arrayBuffer = await response.arrayBuffer()
        if (arrayBuffer.byteLength === 0) {
          throw new Error("Fișierul descărcat este gol.")
        }

        const mammoth = (await import("mammoth")).default
        const result = await mammoth.convertToHtml({ arrayBuffer })

        console.log("[TemplateMappingModal] HTML length:", result.value?.length)
        console.log("[TemplateMappingModal] HTML preview:", result.value?.substring(0, 500))

        if (!result.value || result.value.trim().length === 0) {
          throw new Error("Documentul nu a putut fi convertit în HTML (rezultat gol).")
        }

        setHtmlContent(result.value)
      } catch (err: unknown) {
        console.error("[TemplateMappingModal] error:", err)
        setDocError(err instanceof Error ? err.message : "Eroare necunoscută la încărcarea documentului.")
        toast.error("Eroare la încărcarea template-ului.")
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [open, templateId, token])

  const handleSave = async () => {
    setSaving(true)
    try {
      // Only save fields that were visible in the document preview.
      // If mammoth detected fewer placeholders than the backend field count
      // (e.g. because some placeholders are in headers/footers not rendered by mammoth),
      // the extra fields are excluded so they don't get defaulted to "MANUAL".
      const visibleFieldCount = hasRealContent && detectedCount > 0 ? detectedCount : fields.length
      const payload = fields.slice(0, visibleFieldCount).map((f: TemplateField, idx: number) => {
        const type = mappingTypesRef.current[f.id]
        const isRequired = requiredFieldsRef.current[f.id] ?? true
        let fieldLabel: string
        if (type !== "MANUAL") {
          fieldLabel = type || "MANUAL"
        } else {
          const manualLabel = manualLabelsRef.current[f.id]?.trim()
          if (manualLabel) {
            fieldLabel = manualLabel
          } else if (!isRequired) {
            fieldLabel = `field_for_template_${templateId}_no_${idx + 1}`
          } else {
            fieldLabel = "MANUAL"
          }
        }
        return { fieldId: f.id, fieldLabel, isRequired }
      })

      const res = await updateTemplateMappings(templateId, payload)
      if (res.success) {
        toast.success("Mapările au fost salvate cu succes!")
        setOpen(false)
      } else {
        toast.error("Eroare la salvare: " + res.error)
      }
    } catch {
      toast.error("A apărut o eroare neașteptată la salvare.")
    } finally {
      setSaving(false)
    }
  }

  // Replace placeholders in HTML with custom marker elements
  const processedHtml = useMemo(() => {
    if (!htmlContent) return ""
    let counter = 0
    return htmlContent.replace(PLACEHOLDER_REGEX, () => {
      const field = fields[counter]
      const fieldId = field?.id ?? -1
      counter++
      return `<placeholder-select data-field-id="${fieldId}"></placeholder-select>`
    })
  }, [htmlContent, fields])

  // Count placeholders detected in the document
  const detectedCount = useMemo(() => {
    if (!htmlContent) return 0
    return (htmlContent.match(PLACEHOLDER_REGEX) || []).length
  }, [htmlContent])

  // Check if the document has real contract text (not just placeholders)
  const hasRealContent = useMemo(() => {
    if (!htmlContent) return false
    const stripped = htmlContent
        .replace(/<[^>]+>/g, " ")
        .replace(PLACEHOLDER_REGEX, "")
        .replace(/\s+/g, " ")
        .trim()
    return stripped.length > 50
  }, [htmlContent])

  const allOptions = useMemo(() => {
    const existingValues = new Set(mappingOptions.map((o) => o.value))
    const customLabels = Object.values(manualLabels)
      .map((l) => l.trim())
      .filter((l) => l.length > 0 && !existingValues.has(l))
    const unique = [...new Set(customLabels)]
    return [
      ...mappingOptions,
      ...unique.map((l) => ({ label: l, value: l })),
    ]
  }, [mappingOptions, manualLabels])

  const parserOptions: HTMLReactParserOptions = {
    replace: (domNode) => {
      if (domNode instanceof Element && domNode.name === "placeholder-select") {
        const fieldId = parseInt(domNode.attribs["data-field-id"] ?? "-1")

        if (fieldId === -1) {
          return (
              <span className="inline-block bg-red-100 text-red-600 text-xs px-1 rounded border border-red-300 align-middle mx-0.5">
              [câmp nemapat]
            </span>
          )
        }

        return (
            <span className="inline-flex items-center align-middle mx-0.5 my-0.5 gap-1">
              {(mappingTypes[fieldId] || "MANUAL") === "MANUAL" ? (
                <span className="inline-flex">
                  <input
                    autoComplete="off"
                    className="h-7 w-32 border border-red-200 bg-red-50 rounded-l px-2 text-[11px] outline-none focus:ring-1 focus:ring-red-300 font-medium placeholder:text-gray-400"
                    placeholder="Denumire câmp..."
                    value={manualLabels[fieldId] || ""}
                    onChange={(e) => setManualLabels((prev) => ({ ...prev, [fieldId]: e.target.value }))}
                  />
                  <Select value="" onValueChange={(val) => setMappingTypes((prev) => ({ ...prev, [fieldId]: val }))}>
                    <SelectTrigger className="h-7 w-7 rounded-l-none border-l-0 border-red-200 bg-red-50 px-1 text-slate-400" />
                    <SelectContent className="max-h-72 overflow-y-auto">
                      {allOptions.filter((o) => o.value !== "MANUAL").map((opt) => (
                        <SelectItem key={opt.value} value={opt.value} className="text-[12px]">{opt.label}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </span>
              ) : (
                <Select
                  value={mappingTypes[fieldId]}
                  onValueChange={(val) => setMappingTypes((prev) => ({ ...prev, [fieldId]: val }))}
                >
                  <SelectTrigger className="h-7 min-w-[150px] max-w-[200px] text-[11px] bg-red-50 border-red-200 px-2 focus:ring-1 focus:ring-red-300 font-medium">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent className="max-h-72 overflow-y-auto">
                    {allOptions.map((opt) => (
                      <SelectItem key={opt.value} value={opt.value} className="text-[12px]">{opt.label}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
              <input
                  type="checkbox"
                  className="w-3 h-3 accent-red-500 cursor-pointer shrink-0"
                  checked={requiredFields[fieldId] ?? true}
                  onChange={(e) => setRequiredFields((prev) => ({ ...prev, [fieldId]: e.target.checked }))}
                  title="Câmp obligatoriu"
              />
          </span>
        )
      }
    },
  }

  return (
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogTrigger asChild>
          <Button variant="outline" size="sm">
            <FileEdit className="w-4 h-4 mr-2" />
            Editează Etichetarea
          </Button>
        </DialogTrigger>

        <DialogContent className="max-w-5xl max-h-[92vh] flex flex-col" style={{ width: "90vw" }}>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-base">
              <FileEdit className="w-4 h-4 text-slate-500" />
              Configurare Vizuală: {templateName}
            </DialogTitle>
          </DialogHeader>

          {/* Contextual info banner */}
          <div
              className={`border p-3 rounded-md text-sm flex gap-3 shrink-0 ${
                  hasRealContent
                      ? "bg-slate-50 border-slate-200 text-slate-500"
                      : "bg-slate-50 border-slate-200 text-slate-600"
              }`}
          >
            <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5" />
            <div>
              {hasRealContent ? (
                  <p>
                    Documentul Word este afișat mai jos. Fiecare câmp marcat cu <strong>„....&quot;</strong>{" "}
                    a fost înlocuit cu un dropdown. Alege ce informație trebuie să apară în acel loc.
                  </p>
              ) : (
                  <div>
                    <p className="font-medium">
                      Template-ul nu conține textul contractului, ci doar câmpurile marcate.
                    </p>
                    <p className="mt-1 text-xs">
                      Pentru a vedea documentul complet cu dropdown-urile integrate în text, încarcă un{" "}
                      <strong>.docx</strong> care conține și textul real al contractului (cu{" "}
                      <code>....</code> la locurile de completat). Poți totuși configura etichetarea câmpurilor mai jos.
                    </p>
                  </div>
              )}
            </div>
          </div>

          {/* Document preview area */}
          <div className="flex-1 overflow-y-auto border rounded-md bg-white shadow-inner">
            {loading ? (
                <div className="flex flex-col items-center justify-center h-full py-24 gap-4">
                  <Loader2 className="w-10 h-10 animate-spin text-slate-400" />
                  <p className="text-muted-foreground animate-pulse text-sm">
                    Se convertește documentul Word pentru editare...
                  </p>
                </div>
            ) : docError ? (
                <div className="flex flex-col items-center justify-center h-full py-16 gap-3 px-8 text-center">
                  <AlertCircle className="w-10 h-10 text-red-400" />
                  <p className="text-red-600 font-medium">Nu s-a putut încărca documentul</p>
                  <p className="text-sm text-muted-foreground">{docError}</p>
                  <p className="text-xs text-muted-foreground">
                    Asigurați-vă că microserviciul Java rulează pe portul 8080 și că template-ul este
                    un fișier DOCX valid.
                  </p>
                </div>
            ) : processedHtml && hasRealContent ? (
                /* Full document view with inline dropdowns */
                <div className="contract-preview p-8 max-w-4xl mx-auto">
                  <style
                      dangerouslySetInnerHTML={{
                        __html: `
                  .contract-preview { font-family: 'Times New Roman', Times, serif; font-size: 13px; line-height: 1.7; color: #1a1a1a; }
                  .contract-preview p { margin-bottom: 0.6rem; text-align: justify; }
                  .contract-preview h1 { font-size: 1.2rem; font-weight: bold; text-align: center; margin: 1.2rem 0; }
                  .contract-preview h2 { font-size: 1rem; font-weight: bold; margin: 1rem 0 0.5rem; }
                  .contract-preview h3 { font-size: 0.95rem; font-weight: bold; margin: 0.8rem 0 0.4rem; }
                  .contract-preview strong, .contract-preview b { font-weight: bold; }
                  .contract-preview em, .contract-preview i { font-style: italic; }
                  .contract-preview table { border-collapse: collapse; width: 100%; margin: 12px 0; }
                  .contract-preview td, .contract-preview th { border: 1px solid #ccc; padding: 6px 8px; vertical-align: top; }
                  .contract-preview ul, .contract-preview ol { padding-left: 2rem; margin-bottom: 0.6rem; }
                  .contract-preview li { margin-bottom: 0.3rem; }
                `,
                      }}
                  />
                  {parse(processedHtml, parserOptions)}
                </div>
            ) : fields.length > 0 ? (
                /* Fallback: numbered field mapping list */
                <div className="p-6 space-y-3">
                  <p className="text-xs text-muted-foreground italic mb-4">
                    Configurează etichetarea câmpurilor detectate în ordinea apariției în document:
                  </p>
                  {fields.map((field, idx) => (
                      <div
                          key={field.id}
                          className="flex items-center gap-4 p-3 rounded-lg border bg-gray-50 hover:bg-slate-100 transition-colors"
                      >
                        <div className="flex-shrink-0 w-8 h-8 rounded-full bg-slate-100 text-slate-600 font-bold text-sm flex items-center justify-center">
                          {idx + 1}
                        </div>
                        <div className="flex-1 flex items-center gap-2">
                          {(mappingTypes[field.id] || "MANUAL") === "MANUAL" ? (
                            <div className="flex flex-1">
                              <input
                                autoComplete="off"
                                className="flex-1 h-8 border border-red-200 bg-red-50 rounded-l px-2 text-sm outline-none focus:ring-1 focus:ring-red-300 placeholder:text-gray-400"
                                placeholder="Denumire câmp..."
                                value={manualLabels[field.id] || ""}
                                onChange={(e) => setManualLabels((prev: Record<number, string>) => ({ ...prev, [field.id]: e.target.value }))}
                              />
                              <Select value="" onValueChange={(val) => setMappingTypes((prev) => ({ ...prev, [field.id]: val }))}>
                                <SelectTrigger className="h-8 w-8 rounded-l-none border-l-0 border-red-200 bg-red-50 px-1.5 text-slate-400" />
                                <SelectContent className="max-h-72 overflow-y-auto">
                                  {allOptions.filter((o) => o.value !== "MANUAL").map((opt: TemplateMappingOption) => (
                                    <SelectItem key={opt.value} value={opt.value} className="text-sm">{opt.label}</SelectItem>
                                  ))}
                                </SelectContent>
                              </Select>
                            </div>
                          ) : (
                            <Select
                              value={mappingTypes[field.id]}
                              onValueChange={(val) => setMappingTypes((prev) => ({ ...prev, [field.id]: val }))}
                            >
                              <SelectTrigger className="h-8 text-sm bg-red-50 border-red-200 focus:ring-red-300 flex-1">
                                <SelectValue />
                              </SelectTrigger>
                              <SelectContent className="max-h-72 overflow-y-auto">
                                {allOptions.map((opt: TemplateMappingOption) => (
                                  <SelectItem key={opt.value} value={opt.value} className="text-sm">{opt.label}</SelectItem>
                                ))}
                              </SelectContent>
                            </Select>
                          )}
                          <label className="flex items-center gap-1 text-xs text-muted-foreground shrink-0 cursor-pointer select-none">
                            <input
                                type="checkbox"
                                className="w-3.5 h-3.5 accent-red-500 cursor-pointer"
                                checked={requiredFields[field.id] ?? true}
                                onChange={(e: { target: HTMLInputElement }) =>
                                    setRequiredFields((prev: Record<number, boolean>) => ({ ...prev, [field.id]: e.target.checked }))
                                }
                            />
                            req.
                          </label>
                        </div>
                      </div>
                  ))}
                </div>
            ) : (
                <div className="flex flex-col items-center justify-center h-full py-16 gap-3 text-center">
                  <p className="text-muted-foreground text-sm">Nu există câmpuri de configurat.</p>
                </div>
            )}
          </div>

          <DialogFooter className="pt-3 border-t shrink-0">
            <div className="flex justify-between w-full items-center">
              <p className="text-xs text-muted-foreground">
                {detectedCount > 0
                    ? `${detectedCount} câmpuri detectate în document.`
                    : fields.length > 0
                        ? `${fields.length} câmpuri înregistrate în baza de date.`
                        : "Niciun câmp detectat."}
              </p>
              <div className="flex gap-2">
                <Button variant="outline" onClick={() => setOpen(false)}>
                  Închide
                </Button>
                <Button
                    onClick={handleSave}
                    disabled={loading || saving || fields.length === 0}
                    className="bg-slate-700 hover:bg-slate-800"
                >
                  {saving ? (
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  ) : (
                      <Save className="w-4 h-4 mr-2" />
                  )}
                  Salvează Configurația
                </Button>
              </div>
            </div>
          </DialogFooter>
        </DialogContent>
      </Dialog>
  )
}
