from forge.core.api import BaseAPI, Future
from forge.core.api import api_interface

from services import google_translate_adapter

@api_interface
class GoogleTranslateAdapterAPI(BaseAPI):
    service_name = google_translate_adapter.SERVICE_NAME

    @staticmethod
    def translate_message(userId: str) -> Future[str]:
        """Does something"""