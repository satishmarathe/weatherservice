CREATE TABLE IF NOT EXISTS weather_data (
    id   INTEGER      NOT NULL AUTO_INCREMENT,
    country VARCHAR(128) NOT NULL,
    city VARCHAR(128) NOT NULL,
    weather_details VARCHAR(1024) NOT NULL,
    created_date TIMESTAMP DEFAULT NOW(),
    version INTEGER NOT NULL,
    PRIMARY KEY (id)
);


