import { getTemplates, deleteTemplate } from "@/actions/contract-templates"
import { UploadTemplateDialog } from "@/components/upload-template-dialog"
import { TemplateMappingModal } from "./template-mapping-modal"

export default async function ContractTemplatesPage() {
  const templates = await getTemplates()

  return (
    <div className="p-8 w-full">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Document Templates</h1>
          <p className="text-gray-500 mt-1">Manage your available document templates</p>
        </div>
        <UploadTemplateDialog />
      </div>

      <div className="bg-white shadow overflow-x-auto sm:rounded-lg border border-gray-200">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Nume
              </th>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Fișier
              </th>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Mapat
              </th>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Creat la
              </th>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actualizat la
              </th>
              <th scope="col" className="relative px-6 py-3">
                <span className="sr-only">Acțiuni</span>
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {templates.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-6 py-8 text-center text-gray-500">
                  Niciun template încărcat.
                </td>
              </tr>
            ) : (
              templates.map((template: any) => (
                <tr key={template.id}>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-gray-900">{template.name}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-3">
                      <a href={`/api/templates/download/${template.id}`} target="_blank" rel="noreferrer" className="text-sm text-indigo-600 hover:text-indigo-900 font-medium">
                        Download DOCX
                      </a>
                      <a href={`/api/templates/download/${template.id}/pdf`} target="_blank" rel="noreferrer" className="text-sm text-indigo-600 hover:text-indigo-900 font-medium">
                        Download PDF
                      </a>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-2">
                      {template.fullyMapped && (
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                          Complet ({template.fieldCount} câmpuri)
                        </span>
                      )}
                      <TemplateMappingModal
                        templateId={template.id}
                        templateName={template.name}
                        fullyMapped={template.fullyMapped}
                      />
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {template.createdAt ? new Date(template.createdAt).toLocaleDateString("ro-RO") : "—"}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {template.updatedAt ? new Date(template.updatedAt).toLocaleDateString("ro-RO") : "—"}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <form action={deleteTemplate.bind(null, template.id)}>
                      <button type="submit" className="text-red-600 hover:text-red-900 transition-colors">
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
  )
}
