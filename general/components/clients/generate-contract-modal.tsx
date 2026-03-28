"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { getTemplates } from "@/actions/contract-templates"
import { ContractTemplate } from "@/lib/generated/prisma-client"
import { Label } from "@/components/ui/label"

export function GenerateContractModal({ client }: { client: any }) {
  const [open, setOpen] = useState(false)
  const [templates, setTemplates] = useState<ContractTemplate[]>([])
  
  const [selectedTemplate, setSelectedTemplate] = useState<string>("")
  const [otherOption, setOtherOption] = useState<string>("")

  useEffect(() => {
    if (open) {
      getTemplates().then(setTemplates) // cand se deshide fereastra, incarcam template urile existente
      
      setSelectedTemplate("")
      setOtherOption("")
    }
  }, [open])

  const handleGenerate = async () => {
    console.log("Generare contract cu template-ul", selectedTemplate, "si optiunea secudnara:", otherOption)
    
    setOpen(false)
  }

  // Butonul de "generare contracte" e activ abia cand se alege minim un template
  const isFormValid = selectedTemplate !== ""

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm">gen.</Button>
      </DialogTrigger>
      
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Generează Contract pentru {client.name}</DialogTitle>
        </DialogHeader>
        
        <div className="space-y-6 py-4">
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
          
          {/* TODO : adauga al doilea meniu dropdown*/}
          <div className="space-y-2">
            <Label>A doua opțiune (Urmează să fie definită)</Label>
            <Select value={otherOption} onValueChange={setOtherOption}>
              <SelectTrigger>
                <SelectValue placeholder="Alege o opțiune..." />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="opt1">Opțiune provizorie 1</SelectItem>
                <SelectItem value="opt2">Opțiune provizorie 2</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
        
        <DialogFooter>
          <Button variant="outline" onClick={() => setOpen(false)}>Cancel</Button>
          <Button onClick={handleGenerate} disabled={!isFormValid}>Generează contract</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
