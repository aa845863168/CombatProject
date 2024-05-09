package com.sum.stater.sort

import androidx.collection.ArraySet
import com.sum.stater.task.Task
import com.sum.stater.utils.DispatcherLog

/**
 * 任务排序
 */
object TaskSortUtil {
    // 高优先级的Task
    private val sNewTasksHigh: MutableList<Task> = ArrayList()

    /**
     * 根据任务的依赖关系对其进行排序。
     *
     * @param originTasks 原始任务列表，包含了所有需要进行排序的任务。
     * @param clsLaunchTasks 类任务列表，用于根据任务类找到对应的任务实例。
     * @return 返回一个按照依赖关系排序后的任务列表。
     */
    @Synchronized
    fun getSortResult(
        originTasks: List<Task>,
        clsLaunchTasks: List<Class<out Task>>
    ): List<Task> {
        val makeTime = System.currentTimeMillis() // 记录开始分析任务依赖关系的时间
        val dependSet: MutableSet<Int> = ArraySet() // 用于存储任务依赖关系的集合
        val graph = DirectionGraph(originTasks.size) // 创建有向图，用于表示任务之间的依赖关系

        // 遍历所有任务，构建依赖关系图
        for (i in originTasks.indices) {
            val task = originTasks[i]
            // 如果任务不需要发送或者没有依赖任务，则跳过
            if (task.isSend || task.dependsOn().isNullOrEmpty()) {
                continue
            }

            // 处理任务依赖关系
            task.dependsOn()?.let { list ->
                for (clazz in list) {
                    clazz?.let { cls ->
                        // 查找依赖任务在原始任务列表中的索引
                        val indexOfDepend = getIndexOfTask(originTasks, clsLaunchTasks, cls)
                        // 如果找不到依赖任务，则抛出异常
                        check(indexOfDepend >= 0) {
                            task.javaClass.simpleName +
                                    " depends on " + cls?.simpleName + " can not be found in task list "
                        }
                        // 添加依赖关系到集合中，并在图中添加相应的边
                        dependSet.add(indexOfDepend)
                        graph.addEdge(indexOfDepend, i)
                    }
                }
            }
        }

        // 进行拓扑排序，得到排序后的任务索引列表
        val indexList: List<Int> = graph.topologicalSort()
        // 根据排序结果，获取排序后的任务列表
        val newTasksAll = getResultTasks(originTasks, dependSet, indexList)
        // 记录任务分析所花费的时间
        DispatcherLog.i("task analyse cost makeTime " + (System.currentTimeMillis() - makeTime))
        // 打印所有任务的名称
        printAllTaskName(newTasksAll, false)
        return newTasksAll // 返回排序后的任务列表
    }


    /**
     * 获取最终任务列表
     */
    private fun getResultTasks(
        originTasks: List<Task>,
        dependSet: Set<Int>,
        indexList: List<Int>
    ): List<Task> {
        val newTasksAll: MutableList<Task> = ArrayList(originTasks.size)
        // 被其他任务依赖的
        val newTasksDepended: MutableList<Task> = ArrayList()
        // 没有依赖的
        val newTasksWithOutDepend: MutableList<Task> = ArrayList()
        // 需要提升自己优先级的，先执行（这个先是相对于没有依赖的先）
        val newTasksRunAsSoon: MutableList<Task> = ArrayList()

        for (index in indexList) {
            if (dependSet.contains(index)) {
                newTasksDepended.add(originTasks[index])
            } else {
                val task = originTasks[index]
                if (task.needRunAsSoon()) {
                    newTasksRunAsSoon.add(task)
                } else {
                    newTasksWithOutDepend.add(task)
                }
            }
        }
        // 顺序：被别人依赖的————》需要提升自己优先级的————》需要被等待的————》没有依赖的
        sNewTasksHigh.addAll(newTasksDepended)
        sNewTasksHigh.addAll(newTasksRunAsSoon)
        newTasksAll.addAll(sNewTasksHigh)
        newTasksAll.addAll(newTasksWithOutDepend)
        return newTasksAll
    }

    private fun printAllTaskName(newTasksAll: List<Task>, isPrintName: Boolean) {
        if (!isPrintName) {
            return
        }
        for (task in newTasksAll) {
            DispatcherLog.i(task.javaClass.simpleName)
        }
    }

    val tasksHigh: List<Task>
        get() = sNewTasksHigh

    /**
     * 获取任务在任务列表中的index
     *
     * @param originTasks
     * @param clsLaunchTasks
     * @param cls
     * @return
     */
    private fun getIndexOfTask(
        originTasks: List<Task>,
        clsLaunchTasks: List<Class<out Task>>,
        cls: Class<*>
    ): Int {
        val index = clsLaunchTasks.indexOf(cls)
        if (index >= 0) {
            return index
        }

        // 仅仅是保护性代码
        val size = originTasks.size
        for (i in 0 until size) {
            if (cls.simpleName == originTasks[i].javaClass.simpleName) {
                return i
            }
        }
        return index
    }
}