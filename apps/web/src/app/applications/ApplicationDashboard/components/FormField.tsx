type FormFieldProps = {
  id: string;
  label: string;
  error?: string;
};

export function FormField({ id, label, error }: FormFieldProps) {
  return (
    <div>
      <label htmlFor={id} className="block text-sm font-medium text-slate-700">
        {label}
      </label>
      <input
        id={id}
        name={id}
        type="text"
        required
        maxLength={200}
        className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-950"
      />
      <FieldError message={error} />
    </div>
  );
}

export function FieldError({ message }: { message?: string }) {
  return message ? <p className="mt-1 text-sm text-rose-700">{message}</p> : null;
}
