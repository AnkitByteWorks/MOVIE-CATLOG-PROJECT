import { UserProfileClient } from "./UserProfileClient";

interface UserPageProps {
  params: Promise<{
    username: string;
  }>;
}

export async function generateMetadata({ params }: UserPageProps) {
  const { username } = await params;
  return {
    title: `@${username}'s Watch History | Movie Tracker`,
    description: `Browse @${username}'s movie watches, ratings, and stats on Movie Tracker.`,
  };
}

export default async function UserProfilePage({ params }: UserPageProps) {
  const { username } = await params;
  return <UserProfileClient username={username} />;
}
