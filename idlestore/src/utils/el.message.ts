import {ElMessage} from "element-plus";


export function successMessage(message: string){
    return ElMessage({
        type: 'success',
        message: message
    })
}

export function failMessage(message: string){
    return ElMessage({
        type: 'warning',
        message: message
    })
}
