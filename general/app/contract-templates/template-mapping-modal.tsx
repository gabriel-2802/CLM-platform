"use client"

import { useState, useEffect, useMemo } from "react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { AlertCircle, FileEdit, Loader2, Save } from "lucide-react"
import { getTemplateById, updateTemplateMappings } from "@/actions/contract-templates"
import { toast } from "sonner"
import mammoth from "mammoth"
import parse, { Element, HTMLReactParserOptions } from "html-react-parser"

// Available mapping options
const MAPPING_OPTIONS = [
  { label: "Manual (Input utilizator)", value: "MANUAL" },
  { label: "Nume / Denumire Client", value: "CLIENT_NAME" },
  { label: "CUI Client", value: "CLIENT_CUI" },
  { label: "Adresa Sediu Social", value: "CLIENT_ADDRESS" },
  { label: "Tip Firma (SRL/PFA/etc)", value: "CLIENT_TYPE" },
  { label: "Data Inceput Contract", value: "CONTRACT_START_DATE" },
  { label: "Data Sfarsit Contract", value: "CONTRACT_END_DATE" },
  { label: "Valoare Contract", value: "CONTRACT_VALUE" },
  { label: "Notițe", value: "CONTRACT_NOTES" },
]

export function TemplateMappingModal({ templateId, templateName, fieldCount }: { templateId: number, templateName: string, fieldCount: number }) {
  const [open, setOpen] = useState(false)
  const [fields, setFields] = useState<any[]>([])
  const [htmlContent, setHtmlContent] = useState<string>("")
  const [mappings, setMappings] = useState<Record<number, string>>({})
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)

  // Fetch DOCX and Metadata
  useEffect(() => {
    if (open) {
      setLoading(true)
      
      const fetchData = async () => {
        try {
          // 1. Fetch metadata to get the field IDs and their current labels
          const templateData = await getTemplateById(templateId)
          if (templateData && templateData.fields) {
            setFields(templateData.fields.sort((a: any, b: any) => (a.fieldPosition || 0) - (b.fieldPosition || 0)))
            const initial: Record<number, string> = {}
            templateData.fields.forEach((f: any) => {
              initial[f.id] = f.fieldLabel || "MANUAL"
            })
            setMappings(initial)
          }

          // 2. Fetch the actual DOCX for visual rendering
          const response = await fetch(`/api/templates/download/${templateId}`)
          if (!response.ok) throw new Error("Nu am putut descărca fișierul.")
          
          const arrayBuffer = await response.arrayBuffer()
          const result = await mammoth.convertToHtml({ arrayBuffer })
          setHtmlContent(result.value)
          
        } catch (err) {
          console.error(err)
          toast.error("Eroare la încărcarea template-ului vizual.")
        } finally {
          setLoading(false)
        }
      }

      fetchData()
    }
  }, [open, templateId])

  const handleSave = async () => {
    setSaving(true)
    try {
      const payload = fields.map(f => ({
         fieldId: f.id,
         fieldLabel: mappings[f.id] || "MANUAL"
      }))
      
      const res = await updateTemplateMappings(templateId, payload)
      if (res.success) {
        toast.success("Mapările vizuale au fost salvate!")
        setOpen(false)
      } else {
        toast.error("Eroare la salvare: " + res.error)
      }
    } catch(err) {
      toast.error("A apărut o eroare neașteptată.")
    } finally {
      setSaving(false)
    }
  }

  // Pre-process HTML to insert markers for placeholders
  // We match dots (4 or more) and replace them with a special component marker
  const processedHtml = useMemo(() => {
    if (!htmlContent) return ""
    
    let counter = 0
    // Replace .... sequences with a div that we can identify in the parser
    return htmlContent.replace(/\.{4,}/g, () => {
      const field = fields[counter]
      const fieldId = field?.id || -1
      counter++
      return `<placeholder-select data-field-id="${fieldId}" data-index="${counter-1}"></placeholder-select>`
    })
  }, [htmlContent, fields])

  const options: HTMLReactParserOptions = {
    replace: (domNode) => {
      if (domNode instanceof Element && domNode.name === "placeholder-select") {
        const fieldId = parseInt(domNode.attribs["data-field-id"])
        const index = domNode.attribs["data-index"]
        
        if (fieldId === -1) return <span className="text-red-500 font-bold underline">....</span>

        return (
          <span className="inline-block mx-1 align-middle">
            <Select 
              value={mappings[fieldId] || "MANUAL"} 
              onValueChange={(val) => setMappings(prev => ({ ...prev, [fieldId]: val }))}
            >
              <SelectTrigger className="h-7 min-w-[140px] text-[11px] bg-amber-50 border-amber-300 ring-offset-background p-1 px-2 focus:ring-1 focus:ring-amber-500">
                <SelectValue placeholder="Selectează date..." />
              </SelectTrigger>
              <SelectContent>
                {MAPPING_OPTIONS.map(opt => (
                  <SelectItem key={opt.value} value={opt.value} className="text-[12px]">
                    {opt.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </span>
        )
      }
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm" className="bg-amber-50 text-amber-600 border-amber-200 hover:bg-amber-100 hover:text-amber-700">
          <FileEdit className="w-4 h-4 mr-2" />
          Mapare Vizuală
        </Button>
      </DialogTrigger>

      <DialogContent className="max-w-4xl max-h-[90vh] flex flex-col md:w-[90vw]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            Configurare Vizuală: {templateName}
          </DialogTitle>
        </DialogHeader>

        <div className="flex-1 overflow-y-auto p-4 space-y-4 border rounded-md bg-white">
          <div className="bg-blue-50 border border-blue-100 p-3 rounded-md text-sm text-blue-700 flex gap-3 sticky top-0 z-10 shadow-sm">
             <AlertCircle className="w-5 h-5 flex-shrink-0" />
             <p>Alege din dropdown-uri ce date ale clientului trebuie să apară în fiecare loc marcat din contract. Documentul este redat automat din fișierul Word încărcat.</p>
          </div>

          {loading ? (
             <div className="flex flex-col items-center justify-center py-20 gap-4">
                <Loader2 className="w-10 h-10 animate-spin text-amber-500" />
                <p className="text-muted-foreground animate-pulse">Se convertește documentul pentru editare...</p>
             </div>
          ) : (
             <div className="prose prose-sm max-w-none contract-preview p-6 border-dashed border-2 rounded">
                <style dangerouslySetInnerHTML={{ __html: `
                  .contract-preview p { margin-bottom: 0.5rem; line-height: 1.6; }
                  .contract-preview h1, .contract-preview h2 { font-weight: bold; margin-top: 1rem; }
                  .contract-preview table { border-collapse: collapse; width: 100%; border: 1px solid #ddd; margin: 10px 0; }
                  .contract-preview td, .contract-preview th { border: 1px solid #ddd; padding: 4px; }
                `}} />
                {parse(processedHtml, options)}
             </div>
          )}
        </div>

        <DialogFooter className="pt-4 border-t">
          <div className="flex justify-between w-full items-center">
            <p className="text-xs text-muted-foreground">
              {fieldCount} câmpuri detectate în document.
            </p>
            <div className="flex gap-2">
              <Button variant="outline" onClick={() => setOpen(false)}>Închide</Button>
              <Button onClick={handleSave} disabled={loading || saving} className="bg-green-600 hover:bg-green-700">
                {saving ? <Loader2 className="w-4 h-4 mr-2 animate-spin" /> : <Save className="w-4 h-4 mr-2" />}
                Salvează Configurația
              </Button>
            </div>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
