from environs import Env

env = Env()

GOOGLE_TRANSLATE_API_KEY = env.str('GOOGLE_TRANSLATE_API_KEY')
