import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import Layout from '../components/Layout'
import { useAuth } from '../context/AuthContext'
import { fileAPI } from '../services/api'
import {
  Download,
  Trash2,
  Edit,
  ArrowLeft,
  FileText,
  AlertCircle,
  X,
} from 'lucide-react'

const FileDetails = () => {
  const { publicId } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const fileInputRef = useRef(null)

  const [metadata, setMetadata] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showUpdateModal, setShowUpdateModal] = useState(false)
  const [selectedFile, setSelectedFile] = useState(null)
  const [updating, setUpdating] = useState(false)

  useEffect(() => {
    loadMetadata()
  }, [publicId])

  const loadMetadata = async () => {
    try {
      setLoading(true)
      const response = await fileAPI.getMetadata(publicId)
      setMetadata(response.data)
    } catch (err) {
      setError('Failed to load file details')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleDownload = async () => {
    try {
      const response = await fileAPI.downloadFile(publicId, user.id)

      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', metadata.originalName)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
    } catch (err) {
      alert('Failed to download file')
      console.error(err)
    }
  }

  const handleDelete = async () => {
    if (!confirm(`Are you sure you want to delete "${metadata.originalName}"?`)) {
      return
    }

    try {
      await fileAPI.deleteFile(publicId, user.id)
      navigate('/dashboard')
    } catch (err) {
      alert('Failed to delete file')
      console.error(err)
    }
  }

  const handleFileSelect = (e) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0])
    }
  }

  const handleUpdate = async () => {
    if (!selectedFile) return

    setUpdating(true)
    try {
      await fileAPI.updateFile(publicId, user.id, selectedFile)
      setShowUpdateModal(false)
      setSelectedFile(null)
      loadMetadata() // Reload metadata
      alert('File updated successfully!')
    } catch (err) {
      alert('Failed to update file')
      console.error(err)
    } finally {
      setUpdating(false)
    }
  }

  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
  }

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  if (loading) {
    return (
      <Layout>
        <div className="flex items-center justify-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
      </Layout>
    )
  }

  if (error || !metadata) {
    return (
      <Layout>
        <div className="card">
          <div className="text-center py-12">
            <AlertCircle className="h-16 w-16 text-red-600 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              {error || 'File not found'}
            </h3>
            <button
              onClick={() => navigate('/dashboard')}
              className="btn-primary mt-4"
            >
              Back to Dashboard
            </button>
          </div>
        </div>
      </Layout>
    )
  }

  return (
    <Layout>
      <div className="max-w-6xl mx-auto space-y-6">
        {/* Back Button */}
        <button
          onClick={() => navigate('/dashboard')}
          className="flex items-center space-x-2 text-gray-600 hover:text-gray-900"
        >
          <ArrowLeft className="h-5 w-5" />
          <span>Back to Dashboard</span>
        </button>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* File Preview */}
          <div className="lg:col-span-2 card">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              File Preview
            </h2>

            <div className="border-2 border-gray-200 rounded-lg p-12 bg-gray-50">
              <div className="text-center">
                <FileText className="h-32 w-32 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  {metadata.originalName}
                </h3>
                <p className="text-sm text-gray-600 mb-6">
                  Preview not available for this file type
                </p>
                <button onClick={handleDownload} className="btn-primary">
                  Download to View
                </button>
              </div>
            </div>
          </div>

          {/* File Details */}
          <div className="card space-y-6">
            <h2 className="text-xl font-semibold text-gray-900">File Details</h2>

            <div className="space-y-4">
              <div>
                <label className="text-sm font-medium text-gray-500">Name</label>
                <p className="text-gray-900 mt-1 break-all">
                  {metadata.originalName}
                </p>
              </div>

              <div>
                <label className="text-sm font-medium text-gray-500">
                  Extension
                </label>
                <p className="text-gray-900 mt-1">{metadata.extension}</p>
              </div>

              <div>
                <label className="text-sm font-medium text-gray-500">Size</label>
                <p className="text-gray-900 mt-1">
                  {formatFileSize(metadata.sizeBytes)}
                </p>
              </div>

              <div>
                <label className="text-sm font-medium text-gray-500">Path</label>
                <p className="text-gray-900 mt-1 text-xs break-all">
                  {metadata.storagePath}
                </p>
              </div>

              <div>
                <label className="text-sm font-medium text-gray-500">
                  Last Modified
                </label>
                <p className="text-gray-900 mt-1 text-sm">
                  {formatDate(metadata.updatedAt)}
                </p>
              </div>
            </div>

            {/* Actions */}
            <div className="pt-6 border-t border-gray-200 space-y-3">
              <button
                onClick={handleDownload}
                className="w-full btn-primary flex items-center justify-center space-x-2"
              >
                <Download className="h-5 w-5" />
                <span>Download</span>
              </button>

              <button
                onClick={() => setShowUpdateModal(true)}
                className="w-full btn-secondary flex items-center justify-center space-x-2"
              >
                <Edit className="h-5 w-5" />
                <span>Update File</span>
              </button>

              <button
                onClick={handleDelete}
                className="w-full btn-danger flex items-center justify-center space-x-2"
              >
                <Trash2 className="h-5 w-5" />
                <span>Delete</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Update Modal */}
      {showUpdateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full p-6 space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900">
                Update File
              </h3>
              <button
                onClick={() => {
                  setShowUpdateModal(false)
                  setSelectedFile(null)
                }}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="h-6 w-6" />
              </button>
            </div>

            <div>
              <p className="text-sm text-gray-600 mb-4">
                Select a new file to replace the existing one. The public ID will
                remain unchanged.
              </p>

              <button
                onClick={() => fileInputRef.current?.click()}
                className="w-full btn-secondary"
              >
                Choose File
              </button>
              <input
                ref={fileInputRef}
                type="file"
                onChange={handleFileSelect}
                className="hidden"
                accept=".png,.jpg,.jpeg,.docx,.pdf,.xlsx"
              />

              {selectedFile && (
                <div className="mt-4 p-3 bg-gray-50 rounded-lg">
                  <p className="text-sm font-medium text-gray-900">
                    {selectedFile.name}
                  </p>
                  <p className="text-xs text-gray-600 mt-1">
                    {formatFileSize(selectedFile.size)}
                  </p>
                </div>
              )}
            </div>

            <div className="flex items-center justify-end space-x-3 pt-4 border-t border-gray-200">
              <button
                onClick={() => {
                  setShowUpdateModal(false)
                  setSelectedFile(null)
                }}
                className="btn-secondary"
                disabled={updating}
              >
                Cancel
              </button>
              <button
                onClick={handleUpdate}
                disabled={!selectedFile || updating}
                className="btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {updating ? 'Updating...' : 'Update'}
              </button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  )
}

export default FileDetails
