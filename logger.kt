interface Printable{
    operator fun invoke(str : String);
}

enum class Logger: Printable{
    DEBUG {
        override fun invoke(str : String) {
            if (flag)
                println("Дебуг: ${str}");
        }
    },
    ERROR {
        override fun invoke(str : String) {
            if (flag)
                println("Еррор: ${str}");
        }
    },
    INFO {
        override fun invoke(str : String) {
            if (flag)
                println("Инфо: ${str}");
        }
    };
    companion object {
        var flag : Boolean = true;
    }
}

fun main() {
    Logger.DEBUG("ololololol дебуг");
    Logger.ERROR("ololololol еррор");
    Logger.INFO("ololololol инфо");
}