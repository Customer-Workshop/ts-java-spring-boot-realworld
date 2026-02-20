import { Suspense } from "react";
import BookingForm from "@/components/BookingForm";

export default function BookingPage() {
  return (
    <Suspense fallback={
      <div className="flex items-center justify-center py-20">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-zinc-900"></div>
      </div>
    }>
      <BookingForm />
    </Suspense>
  );
}
