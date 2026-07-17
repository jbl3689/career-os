import { ApplicationDetail } from "./ApplicationDetail";
import { RequireAuthentication } from "@/components/RequireAuthentication";

export default async function ApplicationDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;

  return (
    <RequireAuthentication>
      <ApplicationDetail applicationId={Number(id)} />
    </RequireAuthentication>
  );
}
