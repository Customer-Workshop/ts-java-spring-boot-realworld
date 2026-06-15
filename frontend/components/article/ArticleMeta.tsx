import React from "react";
import Router from "next/router";
import useSWR, { trigger } from "swr";

import ArticleActions from "./ArticleActions";
import CustomImage from "../common/CustomImage";
import CustomLink from "../common/CustomLink";
import ArticleAPI from "../../lib/api/article";
import checkLogin from "../../lib/utils/checkLogin";
import { SERVER_BASE_URL } from "../../lib/utils/constant";
import storage from "../../lib/utils/storage";

const BOOKMARKED_CLASS = "btn btn-sm btn-primary";
const NOT_BOOKMARKED_CLASS = "btn btn-sm btn-outline-primary";

const ArticleMeta = ({ article }) => {
  const { data: currentUser } = useSWR("user", storage);
  const isLoggedIn = checkLogin(currentUser);

  const [bookmarked, setBookmarked] = React.useState(article?.bookmarked || false);

  React.useEffect(() => {
    setBookmarked(article?.bookmarked || false);
  }, [article?.bookmarked]);

  const handleBookmark = async () => {
    if (!isLoggedIn) {
      Router.push(`/user/login`);
      return;
    }

    try {
      if (bookmarked) {
        setBookmarked(false);
        await ArticleAPI.unbookmark(article.slug, currentUser?.token);
      } else {
        setBookmarked(true);
        await ArticleAPI.bookmark(article.slug, currentUser?.token);
      }
      trigger(`${SERVER_BASE_URL}/articles/${article.slug}`);
    } catch (error) {
      setBookmarked(!bookmarked);
    }
  };

  if (!article) return;

  return (
    <div className="article-meta">
      <CustomLink
        href="/profile/[pid]"
        as={`/profile/${encodeURIComponent(article.author?.username)}`}
      >
        <CustomImage src={article.author?.image} alt="author-profile-image" />
      </CustomLink>

      <div className="info">
        <CustomLink
          href="/profile/[pid]"
          as={`/profile/${encodeURIComponent(article.author?.username)}`}
          className="author"
        >
          {article.author?.username}
        </CustomLink>
        <span className="date">
          {new Date(article.createdAt).toDateString()}
        </span>
      </div>

      <ArticleActions article={article} />

      <button
        className={bookmarked ? BOOKMARKED_CLASS : NOT_BOOKMARKED_CLASS}
        onClick={handleBookmark}
        style={{ marginLeft: "4px" }}
      >
        <i className="ion-bookmark" /> {bookmarked ? "Bookmarked" : "Bookmark"}
      </button>
    </div>
  );
};

export default ArticleMeta;
