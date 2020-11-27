package sample

fun main() {
    var someConfig = Config()
    someConfig.title = "++!!!45#6a5sd6as1das12123ad----"
    someConfig.textColor = "#000000"
    someConfig.bgColor = "#FFFFFF"
    someConfig.pos = Notify.Position.RIGHT_BOTTOM
    someConfig.mode = Notify.Modes.DROPDOWN

    someConfig.dropdownOptions = arrayOf("111asd", "222asd")

    Notify.main(someConfig)
}