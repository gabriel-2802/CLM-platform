import { getTemplates, deleteTemplate } from "@/actions/contract-templates"
import { UploadTemplateDialog } from "@/components/upload-template-dialog"
import { TemplateMappingModal } from "./template-mapping-modal"
import { AuthenticatedDownloadLink } from "@/components/authenticated-download-link"

export default async function ContractTemplatesPage() {
  const templates = await getTemplates()

  return (
    <div className="p-6 space-y-4 w-full">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-xl font-semibold text-slate-800">Document Templates</h1>
          <p className="text-sm text-muted-foreground mt-0.5">Gestionează șabloanele de documente disponibile</p>
        </div>
        <UploadTemplateDialog />
      </div>

      <div className="overflow-hidden rounded-lg border border-slate-200 shadow-sm">
        <div className="overflow-x-auto">
          <table className="min-w-full text-xs md:text-sm">
            <thead>
              <tr className="bg-slate-700">
                <th className="px-4 py-2.5 text-left text-xs font-semibold text-slate-100 uppercase tracking-wide whitespace-nowrap">Nume</th>
                <th className="px-4 py-2.5 text-left text-xs font-semibold text-slate-100 uppercase tracking-wide whitespace-nowrap">Fișier</th>
                <th className="px-4 py-2.5 text-left text-xs font-semibold text-slate-100 uppercase tracking-wide whitespace-nowrap">Etichetat</th>
                <th className="px-4 py-2.5 text-left text-xs font-semibold text-slate-100 uppercase tracking-wide whitespace-nowrap">Creat la</th>
                <th className="px-4 py-2.5 text-left text-xs font-semibold text-slate-100 uppercase tracking-wide whitespace-nowrap">Actualizat la</th>
                <th className="px-4 py-2.5 text-right text-xs font-semibold text-slate-100 uppercase tracking-wide whitespace-nowrap">Acțiuni</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-slate-100">
              {templates.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-muted-foreground">
                    Niciun template încărcat.
                  </td>
                </tr>
              ) : (
                templates.map((template: any) => (
                  <tr key={template.id} className="hover:bg-slate-50 transition-colors">
                    <td className="px-4 py-2 whitespace-nowrap font-medium text-slate-800">{template.name}</td>
                    <td className="px-4 py-2 whitespace-nowrap">
                      <div className="flex items-center gap-3">
                        <AuthenticatedDownloadLink
                          path={`/api/templates/download/${template.id}/docx`}
                          label="Download DOCX"
                          className="text-slate-600 hover:text-slate-900 hover:underline"
                          fallbackFilename={`template-${template.id}.docx`}
                        />
                        <AuthenticatedDownloadLink
                          path={`/api/templates/download/${template.id}/pdf`}
                          label="Download PDF"
                          className="text-slate-600 hover:text-slate-900 hover:underline"
                          fallbackFilename={`template-${template.id}.pdf`}
                        />
                      </div>
                    </td>
                    <td className="px-4 py-2 whitespace-nowrap">
                      <div className="flex items-center gap-3">
                        <div className="w-36 flex-shrink-0">
                          {template.fullyMapped ? (
                            <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                              Complet ({template.fieldCount} câmpuri)
                            </span>
                          ) : (
                            <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-700">
                              Incomplet
                            </span>
                          )}
                        </div>
                        <TemplateMappingModal
                          templateId={template.id}
                          templateName={template.name}
                          fullyMapped={template.fullyMapped}
                        />
                      </div>
                    </td>
                    <td className="px-4 py-2 whitespace-nowrap text-slate-500">
                      {template.createdAt ? new Date(template.createdAt).toLocaleDateString("ro-RO") : "—"}
                    </td>
                    <td className="px-4 py-2 whitespace-nowrap text-slate-500">
                      {template.updatedAt ? new Date(template.updatedAt).toLocaleDateString("ro-RO") : "—"}
                    </td>
                    <td className="px-4 py-2 whitespace-nowrap text-right">
                      <form action={deleteTemplate.bind(null, template.id)}>
                        <button type="submit" className="text-red-600 hover:text-red-900 transition-colors text-xs">
                          Șterge
                        </button>
                      </form>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
