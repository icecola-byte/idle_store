Component({
  options:{
    styleIsolation: 'apply-shared',
  },
  properties: {
    checkedIndex: {
      type: Number,
      value: 0
    },
    option: {
      type: Array,
      value: []
    }
  },
  data: {
  },
  methods: {
    handleItemClick(event) {
      const { value } = event.currentTarget.dataset;
      this.triggerEvent('indexChange', {
        value: value,
      });
    }
  }
});