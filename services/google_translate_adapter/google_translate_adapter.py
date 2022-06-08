import logging

from forge.conf import settings
from forge.core.base import BaseService
from forge.core.api import api

from googleapiclient.discovery import build

logger = logging.getLogger(settings.DEFAULT_LOGGER)


class GoogleTranslateAdapter(BaseService):

    def start(self, *args, **kwargs) -> None:
        logger.info("Starting message consumer for GoogleTranslateAdapter...")
        self.start_message_consumer()

    @api
    def do_something(self, userId: str, text: str, source: str, target:str) -> str:
        """
            This function is automatically called when a new message is sent to the service's topic.
        """
        translated_text = None

        try:
            service = build('translate', 'v2', developerKey=settings.GOOGLE_TRANSLATE_API_KEY)

            translation = service.translations().list(
                source=source,
                target=target,
                format='text',
                q=text).execute()

            service.close()

            translated_text = translation["translations"][0]["translatedText"]

        except MutualTLSChannelError:
            logger.debug(
                "Mutual TLS chanel creation has failed or mutual TLS chanel credentials is missing or invalid")
        except Exception as error:
            loger.debug("Error with google translate:" + str(error))

        return translated_text
