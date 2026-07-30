export interface Comments {
  comments: CommentType[];
}

export type CommentType = {
  createdAt: string;
  id: string;
  body: string;
  slug: string;
  author: Author;
  updatedAt: string;
};

export type Author = {
  username: string;
  bio: string;
  image: string;
  following: boolean;
};
