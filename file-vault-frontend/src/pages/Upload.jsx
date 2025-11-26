import { useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import Layout from '../components/Layout'
import { useAuth } from '../context/AuthContext'
import { fileAPI } from '../services/api'
import { Upload as UploadIcon, X, Check, AlertCircle } from 'lucide-react'

const Upload = () => {
  const navigate = useNavigate()
  const { user } = useAuth()
  const fileInputRef = useRef(null)
  
  const [selectedFiles, setSelectedFiles] = useState([])
  const [uploading, setUploading] = useState(false)
  const [uploadProgress, setUploadProgress] = useState({})
  const [error, setError] = useState('')
  const [dragActive, setDragActive] = useState(false)

  const ALLOWED_EXTENSIONS = ['png', 'jpeg', 'jpg', 'docx', 'pdf', 'xlsx']
  const MAX_FILE_SIZE = 5 * 1024 * 1024 // 5MB

  const validateFile = (file) => {
    const extension = file.name.split('.').pop().toLowerCase()
    
    if (!ALLOWED_EXTENSIONS.includes(extension)) {
      return `File type .${extension} is not allowed`
    }
    
    if (file.size > MAX_FILE_SIZE) {
      return 'File size exceeds 5MB limit'
    }
    
    return null
  }

  const handleFiles = (files) => {
    const fileArray = Array.from(files)
    const validFiles = []
    let errors = []

    fileArray.forEach((file) => {
      const error = validateFile(file)
      if (error) {
        errors.push(`${file.name}: ${error}`)
      } else {
        validFiles.push({
          file,
          id: Math.random().toString(36).substr(2, 9),
          status: 'pending',
          progress: 0,
        })
      }
    })

    if (errors.length > 0) {
      setError(errors.join('\n'))
    }

    setSelectedFiles((prev) => [...prev, ...validFiles])
  }

  const handleDrag = (e) => {
    e.preventDefault()
    e.stopPropagation()
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true)
    } else if (e.type === 'dragleave') {
      setDragActive(false)
    }
  }

  const handleDrop = (e) => {
    e.preventDefault()
    e.stopPropagation()
    setDragActive(false)

    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFiles(e.dataTransfer.files)
    }
  }

  const handleFileInput = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      handleFiles(e.target.files)
    }
  }

  const removeFile = (id) => {
    setSelectedFiles((prev) => prev.filter((f) => f.id !== id))
  }

  const uploadFiles = async () => {
    if (selectedFiles.length === 0) return

    setUploading(true)
    setError('')

    for (const fileObj of selectedFiles) {
      if (fileObj.status === 'completed') continue

      try {
        // Update status to uploading
        setSelectedFiles((prev) =>
          prev.map((f) =>
            f.id === fileObj.id ? { ...f, status: 'uploading' } : f
          )
        )

        await fileAPI.uploadFile(user.id, fileObj.file, (progressEvent) => {
          const progress = Math.round(
            (progressEvent.loaded * 100) / progressEvent.total
          )
          setUploadProgress((prev) => ({ ...prev, [fileObj.id]: progress }))
          setSelectedFiles((prev) =>
            prev.map((f) =>
              f.id === fileObj.id ? { ...f, progress } : f
            )
          )
        })

        // Mark as completed
        setSelectedFiles((prev) =>
          prev.map((f) =>
            f.id === fileObj.id ? { ...f, status: 'completed', progress: 100 } : f
          )
        )
      } catch (err) {
        setSelectedFiles((prev) =>
          prev.map((f) =>
            f.id === fileObj.id ? { ...f, status: 'error' } : f
          )
        )
        setError(`Failed to upload ${fileObj.file.name}`)
      }
    }

    setUploading(false)

    // Check if all completed
    const allCompleted = selectedFiles.every((f) => f.status === 'completed')
    if (allCompleted) {
      setTimeout(() => {
        navigate('/dashboard')
      }, 1000)
    }
  }

  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
  }

  return (
    <Layout>
      <div className="max-w-3xl mx-auto space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Upload New File</h1>
          <p className="text-gray-600 mt-1">
            Select or drag files to upload
          </p>
        </div>

        {/* Upload Area */}
        <div className="card">
          <div
            onDragEnter={handleDrag}
            onDragLeave={handleDrag}
            onDragOver={handleDrag}
            onDrop={handleDrop}
            className={`border-2 border-dashed rounded-xl p-12 text-center transition-colors ${
              dragActive
                ? 'border-blue-500 bg-blue-50'
                : 'border-gray-300 hover:border-gray-400'
            }`}
          >
           <UploadIcon className="h-16 w-16 text-blue-600 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              Drag & drop files here or
            </h3>
            <button
              onClick={() => fileInputRef.current?.click()}
              className="btn-primary"
            >
              Browse Files
            </button>
            <input
              ref={fileInputRef}
              type="file"
              multiple
              onChange={handleFileInput}
              className="hidden"
              accept=".png,.jpg,.jpeg,.docx,.pdf,.xlsx"
            />
          </div>

          {/* File Info */}
          <div className="mt-4 text-sm text-gray-600 space-y-1">
            <p>Max file size: 5MB</p>
            <p>Accepted file types: png, jpeg, jpg, docx, pdf, xlsx</p>
          </div>
        </div>

        {/* Error */}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-start space-x-3">
            <AlertCircle className="h-5 w-5 text-red-600 flex-shrink-0 mt-0.5" />
            <p className="text-sm text-red-800 whitespace-pre-line">{error}</p>
          </div>
        )}

        {/* Selected Files */}
        {selectedFiles.length > 0 && (
          <div className="card space-y-4">
            <h3 className="font-semibold text-gray-900">Selected Files</h3>
            
            <div className="space-y-3">
              {selectedFiles.map((fileObj) => (
                <div
                  key={fileObj.id}
                  className="flex items-center space-x-4 p-4 bg-gray-50 rounded-lg"
                >
                  {/* File Info */}
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">
                      {fileObj.file.name}
                    </p>
                    <p className="text-xs text-gray-600">
                      {formatFileSize(fileObj.file.size)}
                    </p>

                    {/* Progress Bar */}
                    {fileObj.status === 'uploading' && (
                      <div className="mt-2">
                        <div className="w-full bg-gray-200 rounded-full h-2">
                          <div
                            className="bg-blue-600 h-2 rounded-full transition-all"
                            style={{ width: `${fileObj.progress}%` }}
                          ></div>
                        </div>
                        <p className="text-xs text-gray-600 mt-1">
                          Uploading... {fileObj.progress}%
                        </p>
                      </div>
                    )}

                    {fileObj.status === 'completed' && (
                      <p className="text-xs text-green-600 mt-1 flex items-center">
                        <Check className="h-4 w-4 mr-1" />
                        Upload Complete
                      </p>
                    )}

                    {fileObj.status === 'error' && (
                      <p className="text-xs text-red-600 mt-1">Upload Failed</p>
                    )}
                  </div>

                  {/* Status Icon */}
                  <div>
                    {fileObj.status === 'pending' && (
                      <button
                        onClick={() => removeFile(fileObj.id)}
                        className="text-gray-400 hover:text-red-600"
                      >
                        <X className="h-5 w-5" />
                      </button>
                    )}
                    {fileObj.status === 'completed' && (
                      <div className="text-green-600">
                        <Check className="h-6 w-6" />
                      </div>
                    )}
                    {fileObj.status === 'uploading' && (
                      <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
                    )}
                  </div>
                </div>
              ))}
            </div>

            {/* Action Buttons */}
            <div className="flex items-center justify-end space-x-3 pt-4 border-t border-gray-200">
              <button
                onClick={() => navigate('/dashboard')}
                className="btn-secondary"
                disabled={uploading}
              >
                Cancel
              </button>
              <button
                onClick={uploadFiles}
                disabled={uploading || selectedFiles.length === 0}
                className="btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {uploading ? 'Uploading...' : `Upload ${selectedFiles.length} File(s)`}
              </button>
            </div>
          </div>
        )}
      </div>
    </Layout>
  )
}

export default Upload
