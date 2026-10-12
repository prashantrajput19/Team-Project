// Turns an HTTP error into a message we can show to the user.
export function describeError(error: any, fallback: string): string {
  // Our backend sends the error message as plain text
  if (typeof error.error === 'string' && error.error) {
    return error.error;
  }
  if (error.status === 0) {
    return 'Unable to connect to the server';
  }
  if (error.status === 403) {
    return 'You are not authorized to perform this action';
  }
  return fallback;
}
