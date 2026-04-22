"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { getTemplates, getTemplateById } from "@/actions/contract-templates"
import { generateContract } from "@/actions/contracts"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { toast } from "sonner"
import { Loader2 } from "lucide-react"

// Labels that are auto-populated by the backend — not shown to the user
const AUTO_LABELS = new Set([
  "CLIENT_NAME",
  "CLIENT_CUI",
  "CLIENT_ADDRESS",
  "CLIENT_TYPE",
  "CLIENT_ADMIN",
  "CONTRACT_START_DATE",
  "CONTRACT_END_DATE",
  "CONTRACT_VALUE",
  "CONTRACT_NOTES",
])

export function GenerateContractModal({ client }: { client: any }) {
  const [open, setOpen] = useState(false)
  const [templates, setTemplates] = useState<any[]>([])
  const [selectedTemplate, setSelectedTemplate] = useState<string>("")
  const [templateFields, setTemplateFields] = useState<any[]>([])
  const [loadingFields, setLoadingFields] = useState(false)
  const [generating, setGenerating] = useState(false)

  // keyed by field.id (number) to avoid collision when multiple MANUAL fields exist
  const [manualValues, setManualValues] = useState<Record<number, string>>({})
  const [startDate, setStartDate] = useState("")
  const [endDate, setEndDate] = useState("")
  const [contractValue, setContractValue] = useState("")
  const [notes, setNotes] = useState("")

  // Load templates on open
  useEffect(() => {
    if (!open) return
    getTemplates().then((all) => {
      // Only show fully mapped templates — unmapped ones can't generate a valid contract
      setTemplates(all.filter((t: any) => t.fullyMapped))
      setSelectedTemplate("")
      setTemplateFields([])
      setManualValues({})
    })
  }, [open])

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
          const fields = res.fields.filter((f: any) => f.fieldLabel)
          setTemplateFields(fields)
          const initial: Record<number, string> = {}
          fields.forEach((f: any) => {
            if (!AUTO_LABELS.has(f.fieldLabel)) {
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

    // Pre-fill dates
    const today = new Date()
    setStartDate(today.toISOString().split("T")[0])
    today.setFullYear(today.getFullYear() + 1)
    setEndDate(today.toISOString().split("T")[0])
    setContractValue("")
    setNotes("")
  }, [selectedTemplate])

  const manualFields = templateFields.filter((f) => !AUTO_LABELS.has(f.fieldLabel))
  const needsValue = templateFields.some((f) => f.fieldLabel === "CONTRACT_VALUE")
  const needsNotes = templateFields.some((f) => f.fieldLabel === "CONTRACT_NOTES")

  const isFormValid =
    selectedTemplate !== "" &&
    startDate !== "" &&
    endDate !== "" &&
    manualFields.every((f) => (f.isRequired ? (manualValues[f.id] ?? "").trim() !== "" : true))

  const handleGenerate = async () => {
    setGenerating(true)
    try {
      const mappings: Record<string, string> = {}

      mappings["CLIENT_NAME"]         = client.name          || ""
      mappings["CLIENT_CUI"]          = client.cui           || ""
      mappings["CLIENT_ADDRESS"]      = client.adresa        || ""
      mappings["CLIENT_TYPE"]         = client.tip           || ""
      mappings["CLIENT_ADMIN"]        = client.administratie || ""
      mappings["CONTRACT_START_DATE"] = startDate
      mappings["CONTRACT_END_DATE"]   = endDate
      mappings["CONTRACT_VALUE"]      = contractValue ? String(parseFloat(contractValue).toFixed(2)) : ""
      mappings["CONTRACT_NOTES"]      = notes || ""

      manualFields.forEach((f) => {
        mappings[f.fieldLabel] = manualValues[f.id] ?? ""
      })

      const payload = {
        templateId: Number(selectedTemplate),
        userId: 1,
        userMail: "admin@clm.com",
        clientId: client.id,
        startDate,
        endDate,
        mappings,
        value: contractValue ? parseFloat(contractValue) : null,
        notes: notes || null,
      }

      const res = await generateContract(payload)
      if (res.success) {
        toast.success("Contract generat cu succes!")
        setOpen(false)
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

      <DialogContent className="max-w-xl max-h-[85vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Generează Contract — {client.name || "Client"}</DialogTitle>
        </DialogHeader>

        <div className="space-y-4 py-4">
          {/* Template selector */}
          <div className="space-y-2">
            <Label>Șablon de contract</Label>
            <Select value={selectedTemplate} onValueChange={setSelectedTemplate}>
              <SelectTrigger>
                <SelectValue placeholder="Alege un șablon mapzat..." />
              </SelectTrigger>
              <SelectContent>
                {templates.length === 0 ? (
                  <SelectItem value="__none__" disabled>
                    Niciun șablon complet mapzat
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
                  Contract Templates
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
              {/* Standard dates */}
              <h4 className="font-medium text-sm text-muted-foreground border-b pb-2">
                Informații standard
              </h4>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>
                    Data de început <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    type="date"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label>
                    Data de sfârșit <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    type="date"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    required
                  />
                </div>
              </div>

              {(needsValue || needsNotes) && (
                <div className="grid grid-cols-2 gap-4">
                  {needsValue && (
                    <div className="space-y-2">
                      <Label>Valoare contract (RON)</Label>
                      <Input
                        type="number"
                        placeholder="ex. 1500"
                        value={contractValue}
                        onChange={(e) => setContractValue(e.target.value)}
                      />
                    </div>
                  )}
                  {needsNotes && (
                    <div className="space-y-2">
                      <Label>Notițe</Label>
                      <Input
                        placeholder="Detalii adiționale..."
                        value={notes}
                        onChange={(e) => setNotes(e.target.value)}
                      />
                    </div>
                  )}
                </div>
              )}

              {/* Auto-filled fields info */}
              <div className="text-xs text-muted-foreground bg-blue-50 border border-blue-100 rounded px-3 py-2">
                Datele clientului (denumire, CUI, adresă, tip, administrator) sunt completate
                automat din baza de date.
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
      </DialogContent>
    </Dialog>
  )
}
