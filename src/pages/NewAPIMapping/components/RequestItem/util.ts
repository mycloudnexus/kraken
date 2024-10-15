export function locationMapping(loc: string): string {
  switch (loc) {
    case 'BODY': return 'Request body'
    case 'QUERY': return 'Query parameter'
    case 'PATH': return 'Path parameter'
    case 'CONSTANT': return 'Constant'
    default: return loc
  }
}
