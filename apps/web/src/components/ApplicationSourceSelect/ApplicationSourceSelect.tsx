import { applicationSources } from "@/lib/applications-api";

type ApplicationSourceSelectProps = {
  id: string;
  name?: string;
  value?: string;
  defaultValue?: string;
  onChange?: (source: string) => void;
};

export function ApplicationSourceSelect({
  id,
  name,
  value,
  defaultValue,
  onChange,
}: ApplicationSourceSelectProps) {
  return (
    <div>
      <label
        htmlFor={id}
        className="block text-sm font-medium text-slate-700"
      >
        Application source
      </label>
      <select
        id={id}
        name={name}
        value={value}
        defaultValue={defaultValue}
        onChange={(event) => onChange?.(event.target.value)}
        className="mt-1 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-slate-950"
      >
        <option value="">Not specified</option>
        {applicationSources.map((source) => (
          <option key={source} value={source}>
            {source}
          </option>
        ))}
      </select>
    </div>
  );
}
