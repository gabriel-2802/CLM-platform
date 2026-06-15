"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import * as DialogPrimitive from "@radix-ui/react-dialog"
import { Dialog, DialogPortal, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
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

export function GenerateContractModal({ client }: { client: ClientForContract }) {
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
  const [localDeLa, setLocalDeLa] = useState<string>("")
  const [localPanaLa, setLocalPanaLa] = useState<string>("")
  const [localTarifConta, setLocalTarifConta] = useState<string>("")
  const [localTarifBilant, setLocalTarifBilant] = useState<string>("")

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
          const src = { ...(freshClient ?? {}), ...client } as Record<string, unknown>
          setLocalDeLa(src.deLa ? String(src.deLa) : "")
          setLocalPanaLa(src.panaLa ? String(src.panaLa) : "")
          setLocalTarifConta(src.tarifConta != null ? String(src.tarifConta) : "")
          setLocalTarifBilant(src.tarifBilant != null ? String(src.tarifBilant) : "")
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
      Boolean(localDeLa) &&
      Boolean(localPanaLa) &&
      Boolean(localTarifBilant) &&
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

    const localOverrides: Record<string, string> = {
      deLa: localDeLa,
      panaLa: localPanaLa,
      tarifConta: localTarifConta,
      tarifBilant: localTarifBilant,
    }
    EXTRA_MAPPING_FIELDS.forEach((field) => {
      mappings[field] = localOverrides[field] ?? ""
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
      const payload = {
        templateId: Number(selectedTemplate),
        clientId: client.id,
        startDate: localDeLa,
        endDate: localPanaLa,
        mappings: buildMappings(),
        autoRenew: true,
        contractBalance: toNumberOrNull(localTarifBilant),
        value: toNumberOrNull(localTarifConta),
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
        <DialogTrigger asChild>
          <Button variant="outline" size="sm">gen.</Button>
        </DialogTrigger>

        <DialogPortal>
        <DialogPrimitive.Overlay className="fixed inset-0 z-50 backdrop-blur-[2px] transition-all duration-500 data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=open]:fade-in-0 data-[state=closed]:fade-out-0" />
        <DialogPrimitive.Content className="fixed left-[50%] top-[50%] z-50 grid w-full max-w-xl translate-x-[-50%] translate-y-[-50%] gap-4 border bg-background p-6 shadow-lg duration-200 rounded-lg max-h-[85vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Genereaza Contract - {client.name || "Client"}</DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-4">
            {/* Template selector */}
            <div className="space-y-2">
              <Label>Șablon de contract</Label>
              <Select value={selectedTemplate} onValueChange={setSelectedTemplate}>
                <SelectTrigger>
                  <SelectValue placeholder="Alege un șablon etichetat..." />
                </SelectTrigger>
                <SelectContent>
                  {templates.length === 0 ? (
                      <SelectItem value="__none__" disabled>
                        Niciun șablon complet etichetat
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
                  {/* Editable contract period and value fields */}
                  <h4 className="font-medium text-sm text-muted-foreground border-b pb-2">
                    Detalii contract
                  </h4>
                  <div className="grid grid-cols-2 gap-3">
                    <div className="space-y-1">
                      <Label className="text-xs">De la <span className="text-red-500">*</span></Label>
                      <Input type="date" value={localDeLa} onChange={(e) => setLocalDeLa(e.target.value)} className="h-8 text-sm" />
                    </div>
                    <div className="space-y-1">
                      <Label className="text-xs">Până la <span className="text-red-500">*</span></Label>
                      <Input type="date" value={localPanaLa} onChange={(e) => setLocalPanaLa(e.target.value)} className="h-8 text-sm" />
                    </div>
                    <div className="space-y-1">
                      <Label className="text-xs">Tarif conta <span className="text-red-500">*</span></Label>
                      <Input type="number" value={localTarifConta} onChange={(e) => setLocalTarifConta(e.target.value)} className="h-8 text-sm" placeholder="0.00" />
                    </div>
                    <div className="space-y-1">
                      <Label className="text-xs">Tarif bilanț <span className="text-red-500">*</span></Label>
                      <Input type="number" value={localTarifBilant} onChange={(e) => setLocalTarifBilant(e.target.value)} className="h-8 text-sm" placeholder="0.00" />
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
                    Câmpurile etichetate la fișa clientului sunt completate automat din baza de date.
                  </div>

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
        </DialogPrimitive.Content>
        </DialogPortal>
      </Dialog>
  )
}
``