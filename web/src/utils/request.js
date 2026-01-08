import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken } from './auth'

const service = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api', // use env or default to proxy
    timeout: 10000
})

// Request interceptor
service.interceptors.request.use(
    config => {
        const token = getToken()
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// Response interceptor
service.interceptors.response.use(
    response => {
        const res = response.data
        // Binary data
        if (response.config.responseType === 'blob') {
            return res
        }
        // Code check
        if (res.code === 0) {
            return res.data
        } else {
            ElMessage.error(res.message || 'Error')
            return Promise.reject(new Error(res.message || 'Error'))
        }
    },
    error => {
        console.error('err' + error)
        ElMessage.error(error.message)
        return Promise.reject(error)
    }
)

export default service
