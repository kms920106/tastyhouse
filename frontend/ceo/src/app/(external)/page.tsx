import { redirect } from "next/navigation";

export default function Home() {
  redirect("/dashboard/shop");
  return <>Coming Soon</>;
}
