Component({
  properties: {
    category: {
      type: Array,
      value: [],
      observer(categories) {
        const displayCategories = (categories || []).map((item, index) => ({
          ...item,
          initial: (item.text || '').slice(0, 1),
          themeIndex: index % 6,
        }));
        this.setData({ displayCategories });
      },
    },
  },
  data: {
    displayCategories: [],
  },
  methods: {
    onTap(e) {
      const id = Number(e.currentTarget.dataset.id);
      if (!Number.isFinite(id)) {
        return;
      }
      this.triggerEvent('change', { value: id });
    },
  },
})
