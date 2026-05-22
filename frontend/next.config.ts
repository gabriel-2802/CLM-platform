import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Required for the production Docker image (general/Dockerfile runner stage)
  output: "standalone",
  eslint: {
    ignoreDuringBuilds: true,
  },
  typescript: {
    ignoreBuildErrors: true,
  },
};

export default nextConfig;
