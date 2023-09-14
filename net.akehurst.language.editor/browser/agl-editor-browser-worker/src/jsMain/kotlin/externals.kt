package net.akehurst.language.editor.worker

import org.w3c.dom.DedicatedWorkerGlobalScope
import org.w3c.dom.SharedWorkerGlobalScope
import org.w3c.dom.WorkerGlobalScope

/**
 * when {
 * Worker -> self : DedicatedWorkerGlobalScope
 * SharedWorker => self : SharedWorkerGlobalScope
 */
external val self: WorkerGlobalScope