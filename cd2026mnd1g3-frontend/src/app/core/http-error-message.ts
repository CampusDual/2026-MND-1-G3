import { HttpErrorResponse } from '@angular/common/http';

interface ErrorPayload {
  detail?: unknown;
  title?: unknown;
  message?: unknown;
}

export function httpErrorMessage(error: unknown, fallback: string): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }

  const payload: unknown = error.error;
  if (typeof payload === 'string' && payload.trim()) {
    return payload;
  }
  if (typeof payload !== 'object' || payload === null) {
    return fallback;
  }

  const body = payload as ErrorPayload;
  for (const candidate of [body.detail, body.title, body.message]) {
    if (typeof candidate === 'string' && candidate.trim()) {
      return candidate;
    }
  }
  return fallback;
}
