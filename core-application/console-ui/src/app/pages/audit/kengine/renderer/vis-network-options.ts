
export const ruleGraphOptions = {
  edges: {
    smooth: true,
    arrows: {
      to: true
    },
    length: 400,
  },
  layout: {
    hierarchical: {
      sortMethod: 'directed',
      nodeSpacing: 400,
      treeSpacing: 150,
      levelSeparation: 150,
      direction: 'UD',
    }
  },
  physics: {
    enabled: false,
  },
  interaction : {
    hover : true
  }
};

export const detailGraphOptions = {
  edges: {
    smooth: true,
    arrows: {
      to: true
    }

  },
  layout: {
    hierarchical: {
      nodeSpacing: 300,
      treeSpacing: 300,
      direction: 'UD'
    }
  },
  physics: {
    enabled: false
  },
  nodes: {
    scaling : {
      label : {
        enabled: true,
        maxVisible: 20
      }
    }
  }
};

