export const applicationQueryKeys = {
  all: ["applications"] as const,
  detail: (id: number) => ["applications", id] as const,
};
