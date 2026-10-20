CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE knowledge_documents (
    id              UUID PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    source_name     VARCHAR(255) NOT NULL,
    content_type    VARCHAR(120) NOT NULL,
    status          VARCHAR(32) NOT NULL,
    error_message   TEXT,
    raw_text        TEXT,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);

CREATE TABLE knowledge_chunks (
    id                  UUID PRIMARY KEY,
    document_id         UUID NOT NULL REFERENCES knowledge_documents (id) ON DELETE CASCADE,
    chunk_index         INT NOT NULL,
    content             TEXT NOT NULL,
    section             VARCHAR(255),
    page_number         INT,
    embedding           vector(384),
    tsv                 tsvector,
    embedding_model     VARCHAR(120) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_knowledge_chunks_document ON knowledge_chunks (document_id);
CREATE INDEX idx_knowledge_chunks_tsv ON knowledge_chunks USING GIN (tsv);

CREATE TABLE ai_conversations (
    id              UUID PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL
);

CREATE TABLE ai_messages (
    id              UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES ai_conversations (id) ON DELETE CASCADE,
    role            VARCHAR(32) NOT NULL,
    content         TEXT NOT NULL,
    tool_trace      TEXT,
    created_at      TIMESTAMPTZ NOT NULL
);

CREATE TABLE chaos_scenarios (
    id              UUID PRIMARY KEY,
    scenario_type   VARCHAR(64) NOT NULL,
    status          VARCHAR(32) NOT NULL,
    parameters_json TEXT,
    result_json     TEXT,
    started_at      TIMESTAMPTZ,
    finished_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL
);
