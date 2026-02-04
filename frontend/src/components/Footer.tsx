import Link from 'next/link';

export default function Footer() {
  return (
    <footer className="bg-gray-50 border-t border-gray-100 mt-auto">
      <div className="max-w-6xl mx-auto px-4 py-8">
        <div className="flex flex-col md:flex-row justify-between items-center gap-4">
          <Link
            href="/"
            className="text-xl font-bold text-emerald-600 hover:text-emerald-700 transition-colors"
          >
            conduit
          </Link>
          <p className="text-gray-500 text-sm">
            An interactive learning project from{' '}
            <a
              href="https://thinkster.io"
              target="_blank"
              rel="noopener noreferrer"
              className="text-emerald-600 hover:underline"
            >
              Thinkster
            </a>
            . Code &amp; design licensed under MIT.
          </p>
        </div>
      </div>
    </footer>
  );
}
