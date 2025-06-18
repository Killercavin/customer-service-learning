CREATE TABLE IF NOT EXISTS topics (
                                      id SERIAL PRIMARY KEY,
                                      title TEXT NOT NULL,
                                      description TEXT,
                                      created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now()
    );