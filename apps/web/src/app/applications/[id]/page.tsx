import { ApplicationDetail } from "./ApplicationDetail";

export default async function ApplicationDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;

  return <ApplicationDetail applicationId={Number(id)} />;
}
