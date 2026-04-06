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

export function GenerateContractModal({ client }: { client: any }) {
  const [open, setOpen] = useState(false)
  const [templates, setTemplates] = useState<any[]>([])

  const [selectedTemplate, setSelectedTemplate] = useState<string>("")
  const [templateFields, setTemplateFields] = useState<any[]>([])
  
  // Form state
  const [mappings, setMappings] = useState<Record<string, string>>({})
  const [startDate, setStartDate] = useState("")
  const [endDate, setEndDate] = useState("")
  const [contractValue, setContractValue] = useState("")
  const [notes, setNotes] = useState("")

  useEffect(() => {
    if (open) {
      getTemplates().then(setTemplates)
      setSelectedTemplate("")
    }
  }, [open])

  useEffect(() => {
    if (selectedTemplate) {
      getTemplateById(Number(selectedTemplate)).then((res) => {
        if (res && res.fields) {
          const fields = res.fields.filter((f: any) => f.fieldLabel);
          setTemplateFields(fields);
          
          // Initial mappings only for manual fields
          const newMappings: Record<string, string> = {};
          fields.forEach((field: any) => {
            const label = field.fieldLabel;
            if (!["CLIENT_NAME", "CLIENT_CUI", "CLIENT_ADDRESS", "CLIENT_TYPE", "CLIENT_ADMIN", "CONTRACT_START_DATE", "CONTRACT_END_DATE", "CONTRACT_VALUE", "CONTRACT_NOTES"].includes(label)) {
              newMappings[label] = "";
            }
          });
          setMappings(newMappings);
        } else {
          setTemplateFields([]);
        }
      }).catch(err => {
        console.error(err);
        setTemplateFields([]);
      });
    } else {
      setTemplateFields([]);
    }
    
    // Reset form when template changes
    const today = new Date();
    const start = today.toISOString().split('T')[0];
    setStartDate(start);
    today.setFullYear(today.getFullYear() + 1);
    const end = today.toISOString().split('T')[0];
    setEndDate(end);
    setContractValue("");
    setNotes("");
  }, [selectedTemplate, client]); // Removed startDate, endDate, contractValue, notes from deps to avoid infinite loop when setting them

  const handleGenerate = async () => {
    try {
      const payload = {
        templateId: Number(selectedTemplate),
        userId: 1, // Dummy user if no auth
        userMail: "miruna.demo@clm.com",
        clientId: client.id || 1, 
        startDate: startDate,
        endDate: endDate,
        mappings: mappings,
        value: contractValue ? parseFloat(contractValue) : null,
        notes: notes
      };

      const res = await generateContract(payload);
      if (res.success) {
        toast.success("Contract generat cu succes!");
        setOpen(false)
      } else {
        toast.error("Eroare generare: " + res.error);
      }
    } catch (err: any) {
      toast.error("A apărut o eroare neașteptată.");
    }
  }

  const isFormValid = selectedTemplate !== "" && templateFields
    .filter(f => !["CLIENT_NAME", "CLIENT_CUI", "CLIENT_ADDRESS", "CLIENT_TYPE", "CLIENT_ADMIN", "CONTRACT_START_DATE", "CONTRACT_END_DATE", "CONTRACT_VALUE", "CONTRACT_NOTES"].includes(f.fieldLabel))
    .every(f => f.isRequired ? !!mappings[f.fieldLabel] : true);

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm">gen.</Button>
      </DialogTrigger>

      <DialogContent className="max-w-xl max-h-[85vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Generează Contract pentru {client.name || "Client"}</DialogTitle>
        </DialogHeader>

        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label>Șablon de contract</Label>
            <Select value={selectedTemplate} onValueChange={setSelectedTemplate}>
              <SelectTrigger>
                <SelectValue placeholder="Alege un șablon existent..." />
              </SelectTrigger>
              <SelectContent>
                {templates.map(t => (
                  <SelectItem key={t.id} value={t.id.toString()}>{t.name}</SelectItem>
                ))}
                {templates.length === 0 && <SelectItem value="none" disabled>Niciun șablon găsit</SelectItem>}
              </SelectContent>
            </Select>
          </div>

          {selectedTemplate && (
            <div className="space-y-4 border rounded-md p-4 bg-muted/20">
              <h4 className="font-medium text-sm text-muted-foreground border-b pb-2">Informații Standard</h4>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Data de început *</Label>
                  <Input type="date" value={startDate} onChange={e => setStartDate(e.target.value)} required />
                </div>
                <div className="space-y-2">
                  <Label>Data de sfârșit *</Label>
                  <Input type="date" value={endDate} onChange={e => setEndDate(e.target.value)} required />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                {templateFields.some(f => f.fieldLabel === "CONTRACT_VALUE") && (
                  <div className="space-y-2">
                    <Label>Valoare contract (RON)</Label>
                    <Input type="number" placeholder="ex. 1500" value={contractValue} onChange={e => setContractValue(e.target.value)} />
                  </div>
                )}
                {templateFields.some(f => f.fieldLabel === "CONTRACT_NOTES") && (
                  <div className="space-y-2">
                    <Label>Notițe</Label>
                    <Input placeholder="Detalii adiționale..." value={notes} onChange={e => setNotes(e.target.value)} />
                  </div>
                )}
              </div>
              
              {templateFields.filter(f => !["CLIENT_NAME", "CLIENT_CUI", "CLIENT_ADDRESS", "CLIENT_TYPE", "CLIENT_ADMIN", "CONTRACT_START_DATE", "CONTRACT_END_DATE", "CONTRACT_VALUE", "CONTRACT_NOTES"].includes(f.fieldLabel)).length > 0 && (
                <>
                  <h4 className="font-medium text-sm text-muted-foreground pt-4 border-t pb-2">Informații adiționale șablon</h4>
                  {templateFields
                    .filter(f => !["CLIENT_NAME", "CLIENT_CUI", "CLIENT_ADDRESS", "CLIENT_TYPE", "CLIENT_ADMIN", "CONTRACT_START_DATE", "CONTRACT_END_DATE", "CONTRACT_VALUE", "CONTRACT_NOTES"].includes(f.fieldLabel))
                    .map(field => (
                    <div key={field.id} className="space-y-2">
                      <Label>{field.fieldLabel} {field.isRequired && <span className="text-red-500">*</span>}</Label>
                      <Input 
                        placeholder={`Introduceți o valoare pentru ${field.fieldLabel}...`}
                        value={mappings[field.fieldLabel] || ""}
                        onChange={(e) => setMappings({ ...mappings, [field.fieldLabel]: e.target.value })}
                        required={field.isRequired}
                      />
                    </div>
                  ))}
                </>
              )}
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => setOpen(false)}>Cancel</Button>
          <Button onClick={handleGenerate} disabled={!isFormValid}>Generează contract</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
