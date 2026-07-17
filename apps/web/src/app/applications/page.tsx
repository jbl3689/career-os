import { ApplicationDashboard } from "./ApplicationDashboard";
import { RequireAuthentication } from "@/components/RequireAuthentication";

export default function ApplicationsPage() {
  return (
    <RequireAuthentication>
      <ApplicationDashboard />
    </RequireAuthentication>
  );
}
