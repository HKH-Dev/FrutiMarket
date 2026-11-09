import { Component, computed, input } from '@angular/core';

/** Ilustracion plana del producto, elegida por su nombre. */
@Component({
  selector: 'app-fruta',
  host: { style: 'display: contents' },
  template: `
    <svg viewBox="0 0 64 64" aria-hidden="true">
      <circle cx="32" cy="32" r="31" fill="#E2F5EF" />
      @switch (tipo()) {
        @case ('mango') {
          <path d="M18 38c-3-13 7-25 21-24 8 1 12 8 10 17-2 11-12 19-22 17-5-1-8-5-9-10Z" fill="#FF9F1C" />
          <path d="M39 14c6 1 11 7 10 17-3-8-6-13-10-17Z" fill="#FFD166" />
          <path d="M38 14c1-4 4-6 8-6-1 4-4 6-8 6Z" fill="#0E7C66" />
        }
        @case ('mora') {
          @for (b of bayas; track $index) { <circle [attr.cx]="b[0]" [attr.cy]="b[1]" r="6.5" fill="#10302A" /><circle [attr.cx]="b[0] - 2" [attr.cy]="b[1] - 2" r="1.6" fill="#E2F5EF" /> }
          <path d="M32 18c-5-1-9-4-11-8 5-1 8 0 11 3 3-3 6-4 11-3-2 4-6 7-11 8Z" fill="#0E7C66" />
        }
        @case ('platano') {
          <path d="M14 18c2 18 15 32 37 30 2 0 3-3 1-4-14-4-24-13-29-28-1-4-9-3-9 2Z" fill="#FFD166" />
          <path d="M14 18c0-4 2-7 4-8l5 5c-3 0-7 1-9 3Z" fill="#0A5A4A" />
          <path d="M20 22c4 11 13 20 26 24" stroke="#FF9F1C" stroke-width="2" fill="none" />
        }
        @case ('cafe') {
          <ellipse cx="26" cy="33" rx="10" ry="15" transform="rotate(-28 26 33)" fill="#10302A" />
          <ellipse cx="41" cy="33" rx="9" ry="14" transform="rotate(24 41 33)" fill="#0A5A4A" />
          <path d="M20 44c7-6 6-17 12-23M36 45c5-7 6-15 10-23" stroke="#FFD166" stroke-width="2" fill="none" />
        }
        @default {
          <path d="M16 46C14 26 28 14 50 14c1 22-11 36-30 34l-4-2Z" fill="#0E7C66" />
          <path d="M16 48c6-12 14-20 26-27" stroke="#FFD166" stroke-width="2" fill="none" />
        }
      }
    </svg>
  `,
})
export class Fruta {
  readonly nombre = input.required<string>();
  protected readonly bayas = [[24, 28], [38, 28], [31, 26], [24, 40], [38, 40], [31, 38], [31, 50]];
  protected readonly tipo = computed(() => {
    const nombre = this.nombre().normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
    return ['mango', 'mora', 'platano', 'cafe'].find((t) => nombre.includes(t)) ?? 'otro';
  });
}
