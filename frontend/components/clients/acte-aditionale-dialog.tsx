"use client"

import { useState, useEffect, useCallback } from "react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Checkbox } from "@/components/ui/checkbox"
import { toast } from "sonner"
import { Loader2, ChevronLeft } from "lucide-react"
import { useRouter } from "next/navigation"
import { useAuthenticatedDownload } from "@/hooks/use-authenticated-download"
import {
    getAppendicesForContract,
    generateAppendix,
    uploadDirectAppendix,
    uploadSignedAppendix,
    terminateAppendix,
    type AppendixSummary,
} from "@/actions/appendices"
import {
    getTemplates,
    getTemplateById,
    getClientTemplateFields,
    type TemplateSummary,
    type TemplateField,
    type TemplateMappingOption,
} from "@/actions/contract-templates"
import { getClientTemplateSource } from "@/actions/clients"

type View = "list" | "upload" | "generate" | "sign"

const EXTRA_MAPPING_FIELDS = ["deLa", "panaLa", "tarifConta", "tarifBilant"]

export type DetailVals = { deLa: string; panaLa: string; tarifConta: string; tarifBilant: string }

export type ClientForAppendix = Record<string, unknown> & {
    id: number
    name?: string
    deLa?: string
    panaLa?: string
    tarifConta?: number
    tarifBilant?: number
}

export function ActeAditionaleDialog({
    contractId,
    client,
    onUpdateDetails,
}: {
    contractId: number
    client: ClientForAppendix
    onUpdateDetails?: (vals: DetailVals) => void
}) {
    const router = useRouter()
    const downloadWithAuth = useAuthenticatedDownload()
    const [open, setOpen] = useState(false)
    const [view, setView] = useState<View>("list")

    // List
    const [appendices, setAppendices] = useState<AppendixSummary[]>([])
    const [loadingList, setLoadingList] = useState(false)
    const [terminatingId, setTerminatingId] = useState<number | null>(null)

    // Sign view (încarcă semnat pentru appendix DRAFT)
    const [signAppendixId, setSignAppendixId] = useState<number | null>(null)
    const [signFile, setSignFile] = useState<File | null>(null)
    const [signUploading, setSignUploading] = useState(false)
    const [signModifica, setSignModifica] = useState(false)
    const [signDeLa, setSignDeLa] = useState("")
    const [signPanaLa, setSignPanaLa] = useState("")
    const [signTarifConta, setSignTarifConta] = useState("")
    const [signTarifBilant, setSignTarifBilant] = useState("")

    // Upload view
    const [uploadTitle, setUploadTitle] = useState("")
    const [uploadFile, setUploadFile] = useState<File | null>(null)
    const [uploading, setUploading] = useState(false)
    const [uploadModifica, setUploadModifica] = useState(false)
    const [uploadDeLa, setUploadDeLa] = useState("")
    const [uploadPanaLa, setUploadPanaLa] = useState("")
    const [uploadTarifConta, setUploadTarifConta] = useState("")
    const [uploadTarifBilant, setUploadTarifBilant] = useState("")

    // Generate view
    const [genTitle, setGenTitle] = useState("")
    const [templates, setTemplates] = useState<TemplateSummary[]>([])
    const [clientFields, setClientFields] = useState<TemplateMappingOption[]>([])
    const [clientTemplateSource, setClientTemplateSource] = useState<Record<string, unknown> | null>(null)
    const [selectedTemplate, setSelectedTemplate] = useState("")
    const [templateFields, setTemplateFields] = useState<TemplateField[]>([])
    const [loadingFields, setLoadingFields] = useState(false)
    const [manualValues, setManualValues] = useState<Record<number, string>>({})
    const [genNotes, setGenNotes] = useState("")
    const [generating, setGenerating] = useState(false)

    const loadAppendices = useCallback(async () => {
        setLoadingList(true)
        const data = await getAppendicesForContract(contractId)
        setAppendices(data)
        setLoadingList(false)
    }, [contractId])

    useEffect(() => {
        if (!open) return
        setView("list")
        loadAppendices()
    }, [open, loadAppendices])

    useEffect(() => {
        if (view !== "generate") return
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
    }, [view, client.id])

    useEffect(() => {
        if (view !== "upload") return
        setUploadDeLa(client.deLa ?? "")
        setUploadPanaLa(client.panaLa ?? "")
        setUploadTarifConta(client.tarifConta != null ? String(client.tarifConta) : "")
        setUploadTarifBilant(client.tarifBilant != null ? String(client.tarifBilant) : "")
        setUploadModifica(false)
    }, [view, client.deLa, client.panaLa, client.tarifConta, client.tarifBilant])

    useEffect(() => {
        if (!selectedTemplate) { setTemplateFields([]); setManualValues({}); return }
        setLoadingFields(true)
        getTemplateById(Number(selectedTemplate))
            .then((res) => {
                if (res?.fields) {
                    const fields = res.fields.filter((f) => f.fieldLabel)
                    setTemplateFields(fields)
                    const initial: Record<number, string> = {}
                    fields.forEach((f) => {
                        if (!clientFields.some((cf) => cf.value === f.fieldLabel)) initial[f.id] = ""
                    })
                    setManualValues(initial)
                } else {
                    setTemplateFields([])
                    setManualValues({})
                }
            })
            .catch(() => setTemplateFields([]))
            .finally(() => setLoadingFields(false))
        setGenNotes("")
    }, [selectedTemplate, clientFields])

    const fmt = (val: string) => {
        const n = parseFloat(val)
        return Number.isFinite(n) ? String(n.toFixed(2)) : ""
    }

    const formatMappingValue = (value: unknown) => {
        if (value == null) return ""
        if (typeof value === "boolean") return value ? "Da" : "Nu"
        if (typeof value === "number") return String(parseFloat(String(value)).toFixed(2))
        return String(value)
    }

    const clientSource = { ...(clientTemplateSource ?? {}), ...client }

    const getClientFieldValue = (fieldName: string) => {
        if (fieldName in clientSource) return clientSource[fieldName]
        const normalized = fieldName.replace(/_/g, "").toLowerCase()
        const key = Object.keys(clientSource).find((k) => k.replace(/_/g, "").toLowerCase() === normalized)
        return key ? clientSource[key] : undefined
    }

    const dynamicClientFieldValues = new Set(clientFields.map((f) => f.value))
    const manualFields = templateFields.filter((f) => !dynamicClientFieldValues.has(f.fieldLabel))

    const buildMappings = (): Record<string, string> => {
        const mappings: Record<string, string> = {}
        clientFields.forEach((f) => { mappings[f.value] = formatMappingValue(getClientFieldValue(f.value)) })
        EXTRA_MAPPING_FIELDS.forEach((field) => { mappings[field] = formatMappingValue(clientSource[field]) })

        manualFields.forEach((f) => { if (f.fieldLabel) mappings[f.fieldLabel] = manualValues[f.id] ?? "" })
        return mappings
    }

    const isGenFormValid =
        genTitle.trim() !== "" &&
        selectedTemplate !== "" &&
        !loadingFields &&
        manualFields.every((f) => (f.isRequired ? (manualValues[f.id] ?? "").trim() !== "" : true))

    const openSignView = (appendixId: number) => {
        setSignAppendixId(appendixId)
        setSignFile(null)
        setSignModifica(false)
        setSignDeLa(client.deLa ?? "")
        setSignPanaLa(client.panaLa ?? "")
        setSignTarifConta(client.tarifConta != null ? String(client.tarifConta) : "")
        setSignTarifBilant(client.tarifBilant != null ? String(client.tarifBilant) : "")
        setView("sign")
    }

    const handleConfirmSign = async () => {
        if (!signFile || signAppendixId == null) return
        setSignUploading(true)
        const fd = new FormData()
        fd.append("file", signFile)
        const res = await uploadSignedAppendix(signAppendixId, fd)
        if (res.success) {
            toast.success("Act adițional semnat încărcat.")
            if (signModifica && onUpdateDetails) {
                onUpdateDetails({ deLa: signDeLa, panaLa: signPanaLa, tarifConta: signTarifConta, tarifBilant: signTarifBilant })
            }
            setView("list")
            loadAppendices()
        } else {
            toast.error("Eroare la încărcare: " + res.error)
        }
        setSignUploading(false)
    }

    const handleTerminate = async (appendixId: number) => {
        setTerminatingId(appendixId)
        const res = await terminateAppendix(appendixId)
        if (res.success) {
            toast.success("Act adițional încheiat.")
            loadAppendices()
        } else {
            toast.error("Eroare la încheiere: " + res.error)
        }
        setTerminatingId(null)
    }

    const handleUpload = async () => {
        if (!uploadFile || !uploadTitle.trim()) return
        setUploading(true)
        const fd = new FormData()
        fd.append("file", uploadFile)
        const res = await uploadDirectAppendix(contractId, uploadTitle.trim(), fd)
        if (res.success) {
            toast.success("Act adițional încărcat.")
            if (uploadModifica && onUpdateDetails) {
                onUpdateDetails({ deLa: uploadDeLa, panaLa: uploadPanaLa, tarifConta: uploadTarifConta, tarifBilant: uploadTarifBilant })
            }
            setUploadTitle("")
            setUploadFile(null)
            setUploadModifica(false)
            setView("list")
            loadAppendices()
        } else {
            toast.error("Eroare la încărcare: " + res.error)
        }
        setUploading(false)
    }

    const handleGenerate = async () => {
        setGenerating(true)
        const res = await generateAppendix({
            contractId,
            templateId: Number(selectedTemplate),
            title: genTitle.trim(),
            notes: genNotes || null,
            mappings: buildMappings(),
        })
        if (res.success) {
            toast.success("Act adițional generat.")
            setGenTitle("")
            setSelectedTemplate("")
            setGenNotes("")
            setView("list")
            loadAppendices()
            router.refresh()
        } else {
            toast.error("Eroare la generare: " + res.error)
        }
        setGenerating(false)
    }

    const detailFields = (
        deLa: string, setDeLa: (v: string) => void,
        panaLa: string, setPanaLa: (v: string) => void,
        tarifConta: string, setTarifConta: (v: string) => void,
        tarifBilant: string, setTarifBilant: (v: string) => void
    ) => (
        <div className="grid grid-cols-2 gap-3 p-3 border rounded-md bg-muted/30">
            <div className="space-y-1">
                <Label>De la <span className="text-red-500">*</span></Label>
                <Input type="date" value={deLa} onChange={(e) => setDeLa(e.target.value)} className="text-slate-900" />
            </div>
            <div className="space-y-1">
                <Label>Pana la <span className="text-red-500">*</span></Label>
                <Input type="date" value={panaLa} onChange={(e) => setPanaLa(e.target.value)} className="text-slate-900" />
            </div>
            <div className="space-y-1">
                <Label>Tarif servicii conta <span className="text-red-500">*</span></Label>
                <Input type="number" value={tarifConta} onChange={(e) => setTarifConta(e.target.value)} className="text-slate-900" />
            </div>
            <div className="space-y-1">
                <Label>Tarif bilant <span className="text-red-500">*</span></Label>
                <Input type="number" value={tarifBilant} onChange={(e) => setTarifBilant(e.target.value)} className="text-slate-900" />
            </div>
        </div>
    )

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <Button variant="outline" size="sm" onClick={() => setOpen(true)}>
                Acte aditionale
            </Button>

            <DialogContent className="max-w-xl max-h-[85vh] overflow-y-auto">
                <DialogHeader>
                    <DialogTitle>
                        {view === "list" && `Acte adiționale - ${client.name ?? ""}`}
                        {view === "upload" && "Încarcă act adițional"}
                        {view === "generate" && "Generează act adițional din șablon"}
                        {view === "sign" && "Încarcă act adițional semnat"}
                    </DialogTitle>
                </DialogHeader>

                {/* ── LIST ── */}
                {view === "list" && (
                    <div className="space-y-4">
                        {loadingList ? (
                            <div className="flex items-center justify-center py-8">
                                <Loader2 className="animate-spin h-5 w-5 text-muted-foreground" />
                            </div>
                        ) : appendices.length === 0 ? (
                            <p className="text-sm text-muted-foreground italic py-4">
                                Nu există acte adiționale pentru acest contract.
                            </p>
                        ) : (
                            <div className="space-y-2">
                                {appendices.map((a) => (
                                    <div
                                        key={a.id}
                                        className="rounded-md border px-3 py-2 text-sm space-y-2"
                                    >
                                        <div className="flex items-start justify-between gap-2">
                                            <div className="space-y-0.5">
                                                <span className="font-medium">{a.title}</span>
                                                {a.templateId != null && (
                                                    <p className="text-xs text-muted-foreground">
                                                        Generat din template
                                                    </p>
                                                )}
                                                <p className={`text-xs font-medium ${
                                                    a.appendixStatus === "SIGNED" ? "text-green-600"
                                                    : a.appendixStatus === "TERMINATED" ? "text-gray-500"
                                                    : "text-red-600"
                                                }`}>
                                                    {a.appendixStatus === "SIGNED" ? "Semnat"
                                                     : a.appendixStatus === "TERMINATED" ? "Încheiat"
                                                     : "Nesemnat"}
                                                </p>
                                            </div>
                                            <div className="flex gap-2 flex-shrink-0">
                                                <Button
                                                    variant="outline"
                                                    size="sm"
                                                    onClick={() =>
                                                        downloadWithAuth(
                                                            `/api/appendices/download/${a.id}/${a.appendixStatus === "SIGNED" ? "signed" : "unsigned"}/pdf`,
                                                            { openInNewTab: true, fallbackFilename: `appendix-${a.id}.pdf` }
                                                        ).catch((err) => {
                                                            const message = err instanceof Error ? err.message : "Descarcare esuata."
                                                            toast.error(message)
                                                        })
                                                    }
                                                >
                                                    {a.appendixStatus === "DRAFT" ? "Descarcă nesemnat" : "Descarcă semnat"}
                                                </Button>
                                                {a.appendixStatus === "DRAFT" && (
                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        onClick={() => openSignView(a.id)}
                                                    >
                                                        Încarcă semnat
                                                    </Button>
                                                )}
                                                {a.appendixStatus === "SIGNED" && (
                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        disabled={terminatingId === a.id}
                                                        onClick={() => handleTerminate(a.id)}
                                                    >
                                                        {terminatingId === a.id ? (
                                                            <><Loader2 className="animate-spin h-4 w-4 mr-1" />Se procesează...</>
                                                        ) : (
                                                            "Încheie"
                                                        )}
                                                    </Button>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}

                        <div className="pt-3 border-t space-y-2">
                            <p className="text-sm font-medium">Act adițional nou</p>
                            <div className="flex gap-2">
                                <Button variant="outline" size="sm" onClick={() => setView("upload")}>
                                    Încarcă act adițional
                                </Button>
                                <Button variant="outline" size="sm" onClick={() => setView("generate")}>
                                    Generează din șablon
                                </Button>
                            </div>
                        </div>
                    </div>
                )}

                {/* ── SIGN ── */}
                {view === "sign" && (
                    <div className="space-y-4 py-2">
                        <div className="space-y-1">
                            <Label>Fișier semnat (PDF / DOCX)</Label>
                            <Input
                                type="file"
                                accept=".pdf,.doc,.docx"
                                onChange={(e) => setSignFile(e.target.files?.[0] ?? null)}
                            />
                        </div>
                        <label className="inline-flex items-center gap-2 text-sm cursor-pointer">
                            <Checkbox
                                checked={signModifica}
                                onCheckedChange={(v) => setSignModifica(v === true)}
                            />
                            <span>Modifica valoarea/data</span>
                        </label>
                        {signModifica && detailFields(
                            signDeLa, setSignDeLa,
                            signPanaLa, setSignPanaLa,
                            signTarifConta, setSignTarifConta,
                            signTarifBilant, setSignTarifBilant
                        )}
                        <DialogFooter className="pt-2">
                            <Button variant="outline" onClick={() => setView("list")} disabled={signUploading}>
                                <ChevronLeft className="h-4 w-4 mr-1" />
                                Înapoi
                            </Button>
                            <Button onClick={handleConfirmSign} disabled={!signFile || signUploading}>
                                {signUploading ? (
                                    <><Loader2 className="animate-spin h-4 w-4 mr-1" />Se încarcă...</>
                                ) : (
                                    "Încarcă"
                                )}
                            </Button>
                        </DialogFooter>
                    </div>
                )}

                {/* ── UPLOAD ── */}
                {view === "upload" && (
                    <div className="space-y-4 py-2">
                        <div className="space-y-1">
                            <Label>Titlu act adițional</Label>
                            <Input
                                placeholder="Ex: Act adițional nr. 1"
                                value={uploadTitle}
                                onChange={(e) => setUploadTitle(e.target.value)}
                            />
                        </div>
                        <div className="space-y-1">
                            <Label>Fișier (PDF / DOCX)</Label>
                            <Input
                                type="file"
                                accept=".pdf,.doc,.docx"
                                onChange={(e) => setUploadFile(e.target.files?.[0] ?? null)}
                            />
                        </div>
                        <label className="inline-flex items-center gap-2 text-sm cursor-pointer">
                            <Checkbox
                                checked={uploadModifica}
                                onCheckedChange={(v) => setUploadModifica(v === true)}
                            />
                            <span>Modifica valoarea/data</span>
                        </label>
                        {uploadModifica && detailFields(
                            uploadDeLa, setUploadDeLa,
                            uploadPanaLa, setUploadPanaLa,
                            uploadTarifConta, setUploadTarifConta,
                            uploadTarifBilant, setUploadTarifBilant
                        )}
                        <DialogFooter className="pt-2">
                            <Button variant="outline" onClick={() => setView("list")}>
                                <ChevronLeft className="h-4 w-4 mr-1" />
                                Înapoi
                            </Button>
                            <Button
                                onClick={handleUpload}
                                disabled={!uploadTitle.trim() || !uploadFile || uploading}
                            >
                                {uploading ? (
                                    <><Loader2 className="animate-spin h-4 w-4 mr-1" />Se încarcă...</>
                                ) : (
                                    "Încarcă"
                                )}
                            </Button>
                        </DialogFooter>
                    </div>
                )}

                {/* ── GENERATE ── */}
                {view === "generate" && (
                    <div className="space-y-4 py-2">
                        <div className="space-y-1">
                            <Label>Titlu act adițional</Label>
                            <Input
                                placeholder="Ex: Act adițional nr. 1"
                                value={genTitle}
                                onChange={(e) => setGenTitle(e.target.value)}
                            />
                        </div>

                        <div className="space-y-1">
                            <Label>Șablon de contract</Label>
                            <Select value={selectedTemplate} onValueChange={setSelectedTemplate}>
                                <SelectTrigger>
                                    <SelectValue placeholder="Alege un șablon mapat..." />
                                </SelectTrigger>
                                <SelectContent>
                                    {templates.map((t) => (
                                        <SelectItem key={t.id} value={String(t.id)}>
                                            {t.name}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>

                        {loadingFields && (
                            <div className="flex items-center gap-2 text-sm text-muted-foreground">
                                <Loader2 className="animate-spin h-4 w-4" />
                                Se încarcă câmpurile șablonului...
                            </div>
                        )}

                        {manualFields.map((f) => (
                            <div key={f.id} className="space-y-1">
                                <Label>
                                    {f.fieldLabel}
                                    {f.isRequired && <span className="text-destructive ml-1">*</span>}
                                </Label>
                                <Input
                                    value={manualValues[f.id] ?? ""}
                                    onChange={(e) =>
                                        setManualValues((prev) => ({ ...prev, [f.id]: e.target.value }))
                                    }
                                />
                            </div>
                        ))}

                        <div className="space-y-1">
                            <Label>Observații</Label>
                            <Input
                                placeholder="Observații opționale..."
                                value={genNotes}
                                onChange={(e) => setGenNotes(e.target.value)}
                            />
                        </div>

                        <DialogFooter className="pt-2">
                            <Button variant="outline" onClick={() => setView("list")}>
                                <ChevronLeft className="h-4 w-4 mr-1" />
                                Înapoi
                            </Button>
                            <Button onClick={handleGenerate} disabled={!isGenFormValid || generating}>
                                {generating ? (
                                    <><Loader2 className="animate-spin h-4 w-4 mr-1" />Se generează...</>
                                ) : (
                                    "Generează"
                                )}
                            </Button>
                        </DialogFooter>
                    </div>
                )}
            </DialogContent>
        </Dialog>
    )
}
