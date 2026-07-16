import {
  applicationStatusLabels,
  applicationStatuses,
} from "@/lib/applications-api";
import type { ApplicationStatus } from "@/lib/applications-api";

type ApplicationStatusSelectProps = {
  id: string;
  name?: string;
  value?: ApplicationStatus;
  defaultValue?: ApplicationStatus;
  onChange?: (status: ApplicationStatus) => void;
};

export function ApplicationStatusSelect({
  id,
  name,
  value,
  defaultValue,
  onChange,
}: ApplicationStatusSelectProps) {
  return (
    <div>
      <label
        htmlFor={id}
        className="block text-sm font-medium text-slate-700"
      >
        Status
      </label>
      <select
        id={id}
        name={name}
        value={value}
        defaultValue={defaultValue}
        onChange={(event) =>
          onChange?.(event.target.value as ApplicationStatus)
        }
        required
        className="mt-1 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-slate-950"
      >
        {applicationStatuses.map((status) => (
          <option key={status} value={status}>
            {applicationStatusLabels[status]}
          </option>
        ))}
      </select>
    </div>
  );
}
