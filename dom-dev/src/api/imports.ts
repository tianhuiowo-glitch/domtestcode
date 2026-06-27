import client from './client'
import type { ApiResponse } from '@/types/auth'

export interface ImportValidationResult {
  tempKey: string
  totalRows: number
  validRows: number
  errorRows: number
  errors: ImportError[]
  previewData: Record<string, string>[]
  columnHeaders?: string[]
}

export interface ImportError {
  row: number
  field: string
  message: string
}

export interface ImportTask {
  taskId: string
  status: 'pending' | 'processing' | 'completed' | 'partial' | 'failed'
  totalRows: number
  processedRows: number
  successRows: number
  failedRows: number
  message: string
}

export const uploadImportFile = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return client
    .post<ApiResponse<ImportValidationResult>>('/imports/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((r) => r.data)
}

export const executeImport = (tempKey: string, selectedRows: number[]) =>
  client
    .post<ApiResponse<ImportTask>>('/imports/execute', { tempKey, selectedRows })
    .then((r) => r.data)

export const getImportTask = (taskId: string) =>
  client.get<ApiResponse<ImportTask>>(`/imports/tasks/${taskId}`).then((r) => r.data)

export interface ImportField {
  value: string
  label: string
  required: boolean
}

export const getImportFields = () =>
  client.get<ApiResponse<ImportField[]>>('/imports/fields').then((r) => r.data)
