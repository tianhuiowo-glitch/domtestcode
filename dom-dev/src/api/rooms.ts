import client from './client'
import type { ApiResponse, PaginatedData } from '@/types/auth'
import type { Room, CreateRoomRequest, UpdateRoomRequest, RoomListParams } from '@/types/room'

export const getRooms = (params: RoomListParams) =>
  client.get<ApiResponse<PaginatedData<Room>>>('/rooms', { params }).then((r) => r.data)

export const getRoom = (id: number) =>
  client.get<ApiResponse<Room>>(`/rooms/${id}`).then((r) => r.data)

export const createRoom = (data: CreateRoomRequest) =>
  client.post<ApiResponse<Room>>('/rooms', data).then((r) => r.data)

export const updateRoom = (id: number, data: UpdateRoomRequest) =>
  client.put<ApiResponse<Room>>(`/rooms/${id}`, data).then((r) => r.data)

export const deleteRoom = (id: number) =>
  client.delete<ApiResponse<null>>(`/rooms/${id}`).then((r) => r.data)

export const getVacantRooms = (dormitoryId: number, date?: string) =>
  client
    .get<ApiResponse<Room[]>>('/rooms/vacant', { params: { dormitoryId, date } })
    .then((r) => r.data)

export const getRoomsByDormitory = (dormitoryId: number) =>
  client
    .get<ApiResponse<PaginatedData<Room>>>('/rooms', { params: { dormitoryId, pageSize: 200 } })
    .then((r) => r.data)
