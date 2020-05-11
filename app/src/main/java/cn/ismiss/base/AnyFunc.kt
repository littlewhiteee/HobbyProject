package cn.ismiss.base

import android.view.View
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

fun View?.setVisible() {
    this?.visibility = View.VISIBLE
}

fun View?.setGone() {
    this?.visibility = View.GONE
}

fun View?.setInVisible() {
    this?.visibility = View.INVISIBLE
}

val Any.TAG: String
    get() = this::class.java.simpleName

fun String?.isEmail(): Boolean {
    println(this)
    return Pattern.compile(""".*@[a-zA-Z0-9]+[.][a-zA-Z0-9]+""")
        .matcher(this).matches()
}

fun String?.isPhone(): Boolean {
    return Pattern.compile("^((13[0-9])|(14[0-9])|(15[0-9])|(16[0-9])|(17[0-9])|(18[0-9])|(19[0-9]))\\d{8}$")
        .matcher(this).matches()
}

fun getTimeStr(time: Long): String {
    val diff = (System.currentTimeMillis() - time) / (1000 * 60 * 60).toFloat()
    val today = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.MINUTE, 0)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.SECOND, 0)
    }.timeInMillis

    return if (time > today) {
        when {
            diff < 1f -> "刚刚"
            else -> "${diff.toInt()} 小时前"
        }
    } else {
        if (time > today - 24 * 60 * 60 * 1000) {
            //昨天
            SimpleDateFormat("昨天 HH:mm", Locale.ENGLISH).format(Date(time))
        } else {
            SimpleDateFormat("MM-dd HH:mm", Locale.ENGLISH).format(Date(time))
        }
    }
}

fun String?.toTimeMiles(fmt: String): Long {
    try {
        return SimpleDateFormat(fmt, Locale.ENGLISH).parse(this).time
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return 0;
}

fun Long.toTime(fmt: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(fmt, Locale.ENGLISH).format(Date(this))
}

interface ICancel {
    val taskList: ArrayList<Disposable>

    fun joinDisposable(disposable: Disposable) {
        taskList.add(disposable)
    }

    fun cancelAllTask() {
        taskList.forEach { if (!it.isDisposed) it.dispose() }
    }
}

fun String.isVideo(): Boolean {
    return arrayOf("mp4", "avi", "mov").contains(File(this).extension.toLowerCase())
}

fun String.isPic(): Boolean {
    return arrayOf("png", "jpg").contains(File(this).extension.toLowerCase())
}

fun <T> Observable<T>.bind(
    cancel: ICancel,
    success: ((T) -> Unit),
    error: ((Throwable) -> Unit)? = null
): Disposable {
    val disposable = this.subscribe({
        success.invoke(it)
    }, { e ->
        e.printStackTrace()
        error?.invoke(e)
    })
    cancel.joinDisposable(disposable)
    return disposable
}

fun <T> createObservable(run: (() -> T?)): Observable<T> {
    return Observable.create<T> {
        val result = run.invoke()
        it.onNext(result!!)
        it.onComplete()
    }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
}

fun String.md5(): String {
    try {
        val instance = MessageDigest.getInstance("MD5")//获取md5加密对象
        val digest = instance.digest(this.toByteArray())//对字符串加密，返回字节数组
        val sb = StringBuffer()
        for (b in digest) {
            val i: Int = b.toInt() and 0xff//获取低八位有效值
            var hexString = Integer.toHexString(i)//将整数转化为16进制
            if (hexString.length < 2) {
                hexString = "0$hexString"//如果是一位的话，补0
            }
            sb.append(hexString)
        }
        return sb.toString()

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}