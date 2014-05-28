package h.d.t.controller;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;

/**
 * Hỗ trợ việc xử lý các thao tác có thời gian xử lý dài (thường là trên 200ms)
 * như load dữ liệu từ server về, nguyên nhân thường do việc tải các dữ liệu từ
 * server về hoặc parse các dữ liệu từ cache mất khá nhiều thời gian, nếu thực
 * hiện trong main thread sẽ làm block ui, ngược lại nếu các màn hình khác nhau
 * đều tự tạo và quản lý thread một cách độc lập thì sẽ khó kiểm soát số lượng
 * thread của toàn bộ ứng dụng, có thể sẽ gây ảnh hưởng cả về memory, network
 * resource...
 * 
 * Với AsyncTaskManager này, mọi request gửi tới đều được đưa vào thread pool để
 * xử lý (xem {@link #mTaskThreadPool}f), số lượng thread sẽ được giới hạn ở mức
 * độ phù hợp, bên cạnh đó có một thread với độ ưu tiên cao dành có các request
 * yêu cầu được thực thi gấp, nếu không thì UI sẽ phải chờ (xem
 * {@link #PRIORITY_BLOCKING})
 * 
 * @author Tran Vu Tat Binh (tranvutatbinh@gmail.com)
 * 
 */
public class AsyncTaskManager {
	/**
	 * Độ ưu tiên bình thường, dành cho các task mà UI không bị block khi chờ
	 * kết quả
	 */
	private static final Object mLock = new Object();

	// Singleton
	private static AsyncTaskManager mInstance;

	/**
	 * Số lượng thread trong thread pool
	 */
	private static final int CORE_NORMAL_POOL_SIZE = 0;

	/**
	 * Số lượng thread tối đa trong thread pool, hiện tại để bằng số lượng trong
	 * trường hợp bình thường để giới hạn tại mức đó luôn
	 */
	private static final int MAXIMUM_NORMAL_POOL_SIZE = 1;

	/**
	 * Thời gian giữ một thread tồn tại để chờ dùng lại sau khi thực thi xong
	 */
	private static final int KEEP_ALIVE_TIME = 2;

	/**
	 * Hàng đợi các task cần thực thi với thread pool
	 */
	private final BlockingQueue<Runnable> mNormalTaskQueue;

	/**
	 * Thread pool để xử lý các task thông thường không đòi hỏi độ ưu tiên cao
	 */
	private final ThreadPoolExecutor mTaskThreadPool;

	public static AsyncTaskManager getInstance(Context context) {
		synchronized (mLock) {
			if (mInstance == null) {
				mInstance = new AsyncTaskManager(
						context.getApplicationContext());
			}
			return mInstance;
		}
	}

	private AsyncTaskManager(Context context) {

		// Những task background có độ ưu tiên trung bình
		mNormalTaskQueue = new LinkedBlockingQueue<Runnable>();
		mTaskThreadPool = new ThreadPoolExecutor(CORE_NORMAL_POOL_SIZE,
				MAXIMUM_NORMAL_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
				mNormalTaskQueue);
		mTaskThreadPool.allowCoreThreadTimeOut(true);

	}

	public void execute(Runnable runnable) {
		mTaskThreadPool.execute(runnable);
	}

	/**
	 * Hoãn thực thi một task nào đó
	 */
	public void cancel(Runnable runnable) {
		mTaskThreadPool.remove(runnable);
	}

}
