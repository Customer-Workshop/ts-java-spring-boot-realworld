import { clsx, type ClassValue } from 'clsx';

export function cn(...inputs: ClassValue[]) {
  return clsx(inputs);
}

export function formatDate(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

export function getErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }
  if (typeof error === 'object' && error !== null) {
    const err = error as { response?: { data?: { errors?: { body?: string[] } } } };
    if (err.response?.data?.errors?.body) {
      return err.response.data.errors.body.join(', ');
    }
  }
  return 'An unexpected error occurred';
}
