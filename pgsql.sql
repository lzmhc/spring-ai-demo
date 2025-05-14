-- public.vector_store definition

-- Drop table

-- DROP TABLE public.vector_store;

CREATE TABLE public.vector_store (
                                     id uuid DEFAULT uuid_generate_v4() NOT NULL,
                                     "content" text NULL,
                                     metadata json NULL,
                                     embedding public.vector NULL,
                                     CONSTRAINT vector_store_pkey PRIMARY KEY (id)
);
CREATE INDEX vector_store_embedding_idx ON public.vector_store USING hnsw (embedding vector_cosine_ops);