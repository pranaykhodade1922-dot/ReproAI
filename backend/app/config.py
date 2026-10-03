from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import Literal

class Settings(BaseSettings):
    HOST: str = "127.0.0.1"
    PORT: int = 8000
    RUNNER_MODE: Literal["mock", "adb"] = "mock"
    NETWORK_STRATEGY: Literal["DETERMINISTIC_DEMO", "REAL_NETWORK"] = "DETERMINISTIC_DEMO"
    ADB_PATH: str = "adb"
    DEFAULT_DEVICE_SERIAL: str = ""
    DEMOSHOP_PACKAGE: str = "com.pranay.demoshop"
    REPROAI_PACKAGE: str = "com.pranay.reproai"
    ENABLE_DEMO_CONTROLS: bool = False

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

settings = Settings()
