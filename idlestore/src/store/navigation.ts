import {defineStore} from "pinia";
export const useToggleMenuStore = defineStore('menuToggle', {
    state: () => {
        return {
            toggle: false,
        }
    },
    actions: {
        toggleMenu(){
            this.toggle = !this.toggle
        },
    }
})