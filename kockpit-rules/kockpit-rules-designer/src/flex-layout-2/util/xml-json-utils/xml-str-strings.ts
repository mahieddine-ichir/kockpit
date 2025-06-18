class Strc {
  xml2json(str: string | undefined) {
    if (typeof str === 'undefined') {
      return ''
    }
    return str.replace(/&#10;/g, '\n')
  }
  json2xml(str: string | undefined) {
    if (typeof str === 'undefined') {
      return ''
    }
    return str.replace(/\\n/g, '&#10;').replace(/\n/g, '&#10;')
  }
}

export const strc = new Strc()
