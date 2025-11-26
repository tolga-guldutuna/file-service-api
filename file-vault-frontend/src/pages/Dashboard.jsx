import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Layout from '../components/Layout'
import { useAuth } from '../context/AuthContext'
import { fileAPI } from '../services/api'
import { 
  Search, 
  Plus, 
  Eye, 
  Download, 
  Trash2, 
  FileText, 
  Image, 
  File,
  AlertCircle 
} from 'lucide-react'

const Dashboard = () => {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [files, setFiles] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    loadFiles()
  }, [])

  const loadFiles = async () => {
    try {
      setLoading(true)
      const response = await fileAPI.listFiles(user.id)
      setFiles(response.data)
    } catch (err) {
      setError('Failed to load files')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleDownload = async (file) => {
    try {
      const response = await fileAPI.downloadFile(file.publicId, user.id)
      
      // Create blob and download
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', file.originalName)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
    } catch (err) {
      alert('Failed to download file')
      console.error(err)
    }
  }

  const handleDelete = async (file) => {
    if (!confirm(`Are you sure you want to delete "${file.originalName}"?`)) {
      return
    }

    try {
      await fileAPI.deleteFile(file.publicId, user.id)
      loadFiles() // Reload list
    } catch (err) {
      alert('Failed to delete file')
      console.error(err)
    }
  }

  const getFileIcon = (extension) => {
    const ext = extension?.toLowerCase()
    if (['png', 'jpg', 'jpeg', 'gif', 'webp'].includes(ext)) {
      return <Image className="h-5 w-5 text-blue-600" />
    }
    if (['pdf', 'doc', 'docx'].includes(ext)) {
      return <FileText className="h-5 w-5 text-red-600" />
    }
    return <File className="h-5 w-5 text-gray-600" />
  }

  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
  }

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    })
  }

  const filteredFiles = files.filter((file) =>
    file.originalName.toLowerCase().includes(searchTerm.toLowerCase())
  )

  return (
    <Layout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
            <p className="text-gray-600 mt-1">
              Manage your files and documents
            </p>
          </div>
          <button
            onClick={() => navigate('/upload')}
            className="btn-primary flex items-center space-x-2 justify-center"
          >
            <Plus className="h-5 w-5" />
            <span>Upload File</span>
          </button>
        </div>

        {/* Search Bar */}
        <div className="card">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              placeholder="Search and filter files..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
            />
          </div>
        </div>

        {/* Error */}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-start space-x-3">
            <AlertCircle className="h-5 w-5 text-red-600 flex-shrink-0 mt-0.5" />
            <p className="text-sm text-red-800">{error}</p>
          </div>
        )}

        {/* Loading */}
        {loading ? (
          <div className="card">
            <div className="flex items-center justify-center py-12">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            </div>
          </div>
        ) : (
          <>
            {/* Files Table */}
            {filteredFiles.length > 0 ? (
              <div className="card overflow-hidden p-0">
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          File Name
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Type
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Size
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Created Date
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Actions
                        </th>
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      {filteredFiles.map((file) => (
                        <tr key={file.publicId} className="hover:bg-gray-50">
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="flex items-center space-x-3">
                              {getFileIcon(file.extension)}
                              <span className="text-sm font-medium text-gray-900">
                                {file.originalName}
                              </span>
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <span className="text-sm text-gray-600">
                              {file.extension}
                            </span>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <span className="text-sm text-gray-600">
                              {formatFileSize(file.sizeBytes)}
                            </span>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <span className="text-sm text-gray-600">
                              {formatDate(file.createdAt)}
                            </span>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="flex items-center space-x-2">
                              <button
                                onClick={() => navigate(`/file/${file.publicId}`)}
                                className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                title="View Details"
                              >
                                <Eye className="h-5 w-5" />
                              </button>
                              <button
                                onClick={() => handleDownload(file)}
                                className="p-2 text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                                title="Download"
                              >
                                <Download className="h-5 w-5" />
                              </button>
                              <button
                                onClick={() => handleDelete(file)}
                                className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                title="Delete"
                              >
                                <Trash2 className="h-5 w-5" />
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* Pagination */}
                <div className="bg-gray-50 px-6 py-4 flex items-center justify-between border-t border-gray-200">
                  <div className="text-sm text-gray-600">
                    Showing {filteredFiles.length} of {files.length} files
                  </div>
                </div>
              </div>
            ) : (
              <div className="card text-center py-12">
                <File className="h-16 w-16 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  {searchTerm ? 'No files found' : 'No files yet'}
                </h3>
                <p className="text-gray-600 mb-6">
                  {searchTerm
                    ? 'Try adjusting your search'
                    : 'Upload your first file to get started'}
                </p>
                {!searchTerm && (
                  <button
                    onClick={() => navigate('/upload')}
                    className="btn-primary inline-flex items-center space-x-2"
                  >
                    <Plus className="h-5 w-5" />
                    <span>Upload File</span>
                  </button>
                )}
              </div>
            )}
          </>
        )}
      </div>
    </Layout>
  )
}

export default Dashboard
