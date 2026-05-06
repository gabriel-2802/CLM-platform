"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { getClientTemplateFields, getTemplates, getTemplateById, type TemplateField, type TemplateMappingOption, type TemplateSummary } from "@/actions/contract-templates"
import { getClientTemplateSource } from "@/actions/clients"
import { generateContract } from "@/actions/contracts"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { toast } from "sonner"
import { Loader2 } from "lucide-react"
import { useRouter } from "next/navigation"

const EXTRA_MAPPING_FIELDS = ["deLa", "panaLa", "tarifConta", "tarifBilant"]

type ClientForContract = Record<string, unknown> & {
  id: number;
  name?: string;
  deLa?: string;
  panaLa?: string;
  tarifConta?: number;
  tarifBilant?: number;
};

export function GenerateContractModal({ client, disabled }: { client: ClientForContract; disabled?: boolean }) {
  const router = useRouter()
  const [open, setOpen] = useState(false)
  const [templates, setTemplates] = useState<TemplateSummary[]>([])
  const [clientFields, setClientFields] = useState<TemplateMappingOption[]>([])
  const [clientTemplateSource, setClientTemplateSource] = useState<Record<string, unknown> | null>(null)
  const [selectedTemplate, setSelectedTemplate] = useState<string>("")
  const [templateFields, setTemplateFields] = useState<TemplateField[]>([])
  const [loadingFields, setLoadingFields] = useState(false)
  const [generating, setGenerating] = useState(false)
  const [manualValues, setManualValues] = useState<Record<number, string>>({})
  const [notes, setNotes] = useState("")

  // Load templates on open
  useEffect(() => {
    if (!open) return
    Promise.all([getTemplates(), getClientTemplateFields(), getClientTemplateSource(client.id)]).then(
        ([allTemplates, fields, freshClient]) => {
          setTemplates(allTemplates.filter((t) => t.fullyMapped))
          setClientFields(fields)
          setClientTemplateSource(freshClient)
          setSelectedTemplate("")
          setTemplateFields([])
          setManualValues({})
        }
    )
  }, [client.id, open])

  // Load fields when template changes
  useEffect(() => {
    if (!selectedTemplate) {
      setTemplateFields([])
      setManualValues({})
      return
    }

    setLoadingFields(true)
    getTemplateById(Number(selectedTemplate))
        .then((res) => {
          if (res?.fields) {
            const fields = res.fields.filter((f) => f.fieldLabel)
            setTemplateFields(fields)
            const initial: Record<number, string> = {}
            fields.forEach((f) => {
              if (!clientFields.some((field) => field.value === f.fieldLabel)) {
                initial[f.id] = ""
              }
            })
            setManualValues(initial)
          } else {
            setTemplateFields([])
            setManualValues({})
          }
        })
        .catch(() => setTemplateFields([]))
        .finally(() => setLoadingFields(false))

    setNotes("")
  }, [clientFields, selectedTemplate])

  const dynamicClientFieldValues = new Set(clientFields.map((field) => field.value))
  const clientSource = { ...(clientTemplateSource ?? {}), ...client }
  const manualFields = templateFields.filter((f) => !dynamicClientFieldValues.has(f.fieldLabel))

  const isFormValid =
      selectedTemplate !== "" &&
      !loadingFields &&
      Boolean(clientSource.deLa) &&
      Boolean(clientSource.panaLa) &&
      manualFields.every((f) => (f.isRequired ? (manualValues[f.id] ?? "").trim() !== "" : true))

  const fmt = (val: number | undefined | null) =>
      val != null ? String(parseFloat(String(val)).toFixed(2)) : ""

  const formatMappingValue = (value: unknown) => {
    if (value == null) return ""
    if (typeof value === "boolean") return value ? "Da" : "Nu"
    if (typeof value === "number") return fmt(value)
    return String(value)
  }

  const toNumberOrNull = (value: unknown) => {
    if (typeof value === "number") return Number.isFinite(value) ? value : null
    if (typeof value !== "string" || !value.trim()) return null
    const parsed = Number(value)
    return Number.isFinite(parsed) ? parsed : null
  }

  const getClientFieldValue = (fieldName: string) => {
    if (fieldName in clientSource) return clientSource[fieldName]

    const normalizedFieldName = fieldName.replace(/_/g, "").toLowerCase()
    const sourceKey = Object.keys(clientSource).find(
        (key) => key.replace(/_/g, "").toLowerCase() === normalizedFieldName
    )

    return sourceKey ? clientSource[sourceKey] : undefined
  }

  const buildMappings = () => {
    const mappings: Record<string, string> = {}

    clientFields.forEach((field) => {
      mappings[field.value] = formatMappingValue(getClientFieldValue(field.value))
    })

    EXTRA_MAPPING_FIELDS.forEach((field) => {
      mappings[field] = formatMappingValue(clientSource[field])
    })

    manualFields.forEach((field) => {
      if (!field.fieldLabel) return
      mappings[field.fieldLabel] = manualValues[field.id] ?? ""
    })

    return mappings
  }

  const handleGenerate = async () => {
    setGenerating(true)
    try {
      const contractBalance = toNumberOrNull(clientSource.tarifBilant)
      if (contractBalance === null) {
        toast.error("Tarif bilanț este obligatoriu pentru generare contract")
        setGenerating(false)
        return
      }

      const payload = {
        templateId: Number(selectedTemplate),
        clientId: client.id,
        startDate: String(clientSource.deLa || ""),
        endDate: String(clientSource.panaLa || ""),
        mappings: buildMappings(),
        autoRenew: true,
        contractBalance,
        value: toNumberOrNull(clientSource.tarifConta),
        notes: notes || null,
      }

      const res = await generateContract(payload)
      if (res.success) {
        toast.success("Contract generat cu succes!")
        setOpen(false)
        router.refresh()
      } else {
        toast.error("Eroare la generare: " + res.error)
      }
    } catch {
      toast.error("A apărut o eroare neașteptată.")
    } finally {
      setGenerating(false)
    }
  }

  return (
      <Dialog open={open} onOpenChange={setOpen}>
        <Tooltip>
          <TooltipTrigger asChild>
          <span className="inline-flex">
            <DialogTrigger asChild>
              <Button variant="outline" size="sm" disabled={disabled}>gen.</Button>
            </DialogTrigger>
          </span>
          </TooltipTrigger>
          {disabled && (
              <TooltipContent side="top" className="max-w-xs text-center">
                Nu se poate genera contract deoarece nu au fost completate toate campurile din sectiunea Detalii
              </TooltipContent>
          )}
        </Tooltip>

        <DialogContent className="max-w-xl max-h-[85vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Genereaza Contract - {client.name || "Client"}</DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-4">
            {/* Template selector */}
            <div className="space-y-2">
              <Label>Șablon de contract</Label>
              <Select value={selectedTemplate} onValueChange={setSelectedTemplate}>
                <SelectTrigger>
                  <SelectValue placeholder="Alege un șablon mapat..." />
                </SelectTrigger>
                <SelectContent>
                  {templates.length === 0 ? (
                      <SelectItem value="__none__" disabled>
                        Niciun șablon complet mapat
                      </SelectItem>
                  ) : (
                      templates.map((t) => (
                          <SelectItem key={t.id} value={t.id.toString()}>
                            {t.name}
                          </SelectItem>
                      ))
                  )}
                </SelectContent>
              </Select>
              {templates.length === 0 && (
                  <p className="text-xs text-muted-foreground">
                    Încarcă și mapează un template în pagina{" "}
                    <a href="/contract-templates" className="text-indigo-600 underline">
                      Document Templates
                    </a>{" "}
                    mai întâi.
                  </p>
              )}
            </div>

            {/* Fields loaded from selected template */}
            {loadingFields && (
                <div className="flex items-center gap-2 text-sm text-muted-foreground py-2">
                  <Loader2 className="w-4 h-4 animate-spin" />
                  Se încarcă câmpurile...
                </div>
            )}

            {selectedTemplate && !loadingFields && (
                <div className="space-y-4 border rounded-md p-4 bg-muted/20">
                  {/* Summary of auto-filled values from client row */}
                  <h4 className="font-medium text-sm text-muted-foreground border-b pb-2">
                    Informații preluate din fișa clientului
                  </h4>
                  <div className="grid grid-cols-2 gap-2 text-sm">
                    <div>
                      <span className="text-muted-foreground">De la: </span>
                      <span className="font-medium">{clientSource.deLa || "—"}</span>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Până la: </span>
                      <span className="font-medium">{clientSource.panaLa || "—"}</span>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Tarif conta: </span>
                      <span className="font-medium">{clientSource.tarifConta != null ? clientSource.tarifConta : "—"}</span>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Tarif bilanț: </span>
                      <span className="font-medium">{clientSource.tarifBilant != null ? clientSource.tarifBilant : "—"}</span>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label>Notițe</Label>
                    <Input
                        placeholder="Detalii adiționale..."
                        value={notes}
                        onChange={(e) => setNotes(e.target.value)}
                    />
                  </div>

                  {/* Auto-filled fields info */}
                  <div className="text-xs text-muted-foreground bg-blue-50 border border-blue-100 rounded px-3 py-2">
                    Câmpurile mapate la fișa clientului sunt completate
                    automat din baza de date.
                  </div>
                  {(!clientSource.deLa || !clientSource.panaLa) && (
                      <div className="text-xs text-red-700 bg-red-50 border border-red-100 rounded px-3 py-2">
                        Completează câmpurile De la și Până la în tabel înainte de generare.
                      </div>
                  )}

                  {/* Manual / custom fields */}
                  {manualFields.length > 0 && (
                      <>
                        <h4 className="font-medium text-sm text-muted-foreground pt-2 border-t pb-2">
                          Câmpuri suplimentare
                        </h4>
                        {manualFields.map((field, idx) => {
                          const label =
                              field.fieldLabel === "MANUAL"
                                  ? `Câmp manual #${idx + 1}`
                                  : field.fieldLabel
                          return (
                              <div key={field.id} className="space-y-2">
                                <Label>
                                  {label}{" "}
                                  {field.isRequired && <span className="text-red-500">*</span>}
                                </Label>
                                <Input
                                    placeholder={`Introduceți valoarea pentru ${label}...`}
                                    value={manualValues[field.id] ?? ""}
                                    onChange={(e) =>
                                        setManualValues((prev) => ({ ...prev, [field.id]: e.target.value }))
                                    }
                                    required={field.isRequired}
                                />
                              </div>
                          )
                        })}
                      </>
                  )}
                </div>
            )}
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>
              Anulează
            </Button>
            <Button onClick={handleGenerate} disabled={!isFormValid || generating}>
              {generating ? (
                  <>
                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                    Se generează...
                  </>
              ) : (
                  "Generează contract"
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
  )
}
``